package org.burgas.backendserver.service;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.identity.IdentityRequest;
import org.burgas.backendserver.dto.identity.IdentityResponse;
import org.burgas.backendserver.entity.Identity;
import org.burgas.backendserver.entity.Image;
import org.burgas.backendserver.exception.*;
import org.burgas.backendserver.mapper.IdentityMapper;
import org.burgas.backendserver.message.IdentityMessages;
import org.burgas.backendserver.repository.identity.IdentityMasterRepository;
import org.burgas.backendserver.repository.identity.IdentityReplicaRepository;
import org.burgas.backendserver.repository.image.ImageMasterRepository;
import org.burgas.backendserver.repository.image.ImageReplicaRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS, transactionManager = "replicaPostgresTransactionManager")
public class IdentityService {

    private final IdentityMasterRepository identityMasterRepository;
    private final IdentityReplicaRepository identityReplicaRepository;

    private final IdentityMapper identityMapper;
    private final PasswordEncoder passwordEncoder;

    private final ImageService imageService;
    private final ImageMasterRepository imageMasterRepository;
    private final ImageReplicaRepository imageReplicaRepository;

    private final BeanFactory beanFactory;
    private final BucketService bucketService;

    public List<IdentityResponse> findAll() {
        return this.identityReplicaRepository.findAll()
                .stream()
                .map(this.identityMapper::toResponse)
                .collect(Collectors.toList());
    }

    public IdentityResponse findById(final UUID identityId) {
        return this.identityReplicaRepository.findById(identityId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId)
                .map(this.identityMapper::toResponse)
                .orElseThrow(
                        () -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage())
                );
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public IdentityResponse createInMaster(final IdentityRequest identityRequest) {
        identityRequest.setId(null);
        UUID identityId = UUID.randomUUID();

        while (this.identityReplicaRepository.existsById(identityId) && this.identityReplicaRepository.existsById(identityId))
            identityId = UUID.randomUUID();

        Identity identityMaster = this.identityMapper.toEntityMaster(identityRequest);
        identityMaster.setId(identityId);
        Identity saved = this.identityMasterRepository.save(identityMaster);

        this.beanFactory.getBean(IdentityService.class).createInReplica(identityId, identityRequest);
        this.bucketService.createBucketInMaster(saved);

        return this.identityMapper.toResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void createInReplica(final UUID identityId, final IdentityRequest identityRequest) {
        Identity identityReplica = this.identityMapper.toEntityReplica(identityRequest);
        identityReplica.setId(identityId);
        this.identityReplicaRepository.save(identityReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public IdentityResponse updateInMaster(final IdentityRequest identityRequest, final UUID identityId) {
        if (identityId == null)
            throw new IdEmptyException(IdentityMessages.IDENTITY_ID_EMPTY.getMessage());

        identityRequest.setId(identityId);

        Identity identityMaster = this.identityMapper.toEntityMaster(identityRequest);
        Identity saved = this.identityMasterRepository.save(identityMaster);

        this.beanFactory.getBean(IdentityService.class).updateInReplica(identityRequest);

        return this.identityMapper.toResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void updateInReplica(final IdentityRequest identityRequest) {
        Identity identityReplica = this.identityMapper.toEntityReplica(identityRequest);
        this.identityReplicaRepository.save(identityReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String deleteFromMaster(final UUID identityId) {
        if (identityId == null)
            throw new IdEmptyException(IdentityMessages.IDENTITY_ID_EMPTY.getMessage());

        Identity identityMaster = this.identityMasterRepository.findById(identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));
        this.identityMasterRepository.delete(identityMaster);

        this.beanFactory.getBean(IdentityService.class).deleteFromReplica(identityId);

        return IdentityMessages.IDENTITY_DELETED.getMessage();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void deleteFromReplica(final UUID identityId) {
        Identity identityReplica = this.identityReplicaRepository.findById(identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));
        this.identityReplicaRepository.delete(identityReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String changePasswordInMaster(final UUID identityId, final String password) {
        if (identityId == null)
            throw new IdEmptyException(IdentityMessages.IDENTITY_ID_EMPTY.getMessage());

        if (password == null || password.isBlank())
            throw new IdentityPasswordEmptyException(IdentityMessages.IDENTITY_PASSWORD_EMPTY.getMessage());


        Identity identityMaster = this.identityMasterRepository.findById(identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));


        if (this.passwordEncoder.matches(password, identityMaster.getPassword()))
            throw new IdentityPasswordMatchesException(IdentityMessages.IDENTITY_PASSWORD_MATCHES.getMessage());

        String newPassword = this.passwordEncoder.encode(password);
        identityMaster.setPassword(newPassword);
        this.identityMasterRepository.save(identityMaster);

        this.beanFactory.getBean(IdentityService.class).changePasswordInReplica(identityId, password, newPassword);

        return IdentityMessages.IDENTITY_PASSWORD_CHANGED.getMessage();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void changePasswordInReplica(final UUID identityId, final String password, final String newPassword) {
        Identity identityReplica = this.identityReplicaRepository.findById(identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        if (this.passwordEncoder.matches(password, identityReplica.getPassword()))
            throw new IdentityPasswordMatchesException(IdentityMessages.IDENTITY_PASSWORD_MATCHES.getMessage());

        identityReplica.setPassword(newPassword);
        this.identityReplicaRepository.save(identityReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String enableDisableOnMaster(final UUID identityId, final Boolean enabled) {
        if (identityId == null)
            throw new IdEmptyException(IdentityMessages.IDENTITY_ID_EMPTY.getMessage());

        Identity identityMaster = this.identityMasterRepository.findById(identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));


        if (identityMaster.getEnabled().equals(enabled))
            throw new IdentityEnableMatchException(IdentityMessages.IDENTITY_ENABLE_MATCH.getMessage());

        identityMaster.setEnabled(enabled);
        this.identityMasterRepository.save(identityMaster);

        this.beanFactory.getBean(IdentityService.class).enableDisableOnReplica(identityId, enabled);

        return enabled == true ?
                IdentityMessages.IDENTITY_ENABLED.getMessage() : IdentityMessages.IDENTITY_DISABLED.getMessage();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void enableDisableOnReplica(final UUID identityId, final Boolean enabled) {
        Identity identityReplica = this.identityReplicaRepository.findById(identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        if (identityReplica.getEnabled().equals(enabled))
            throw new IdentityEnableMatchException(IdentityMessages.IDENTITY_ENABLE_MATCH.getMessage());

        identityReplica.setEnabled(enabled);
        this.identityReplicaRepository.save(identityReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String uploadImageOnMaster(final UUID identityId, final MultipartFile multipartFile) throws IOException {
        UUID identityUUID = identityId == null ? UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId;

        Identity identityMaster = this.identityMasterRepository.findById(identityUUID)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        Image image = this.imageService.uploadToMaster(multipartFile);
        image.setPreview(true);
        this.imageMasterRepository.save(image);

        identityMaster.setImage(image);
        this.identityMasterRepository.save(identityMaster);

        this.beanFactory.getBean(IdentityService.class).uploadImageOnReplica(identityUUID, image);

        return IdentityMessages.IDENTITY_IMAGE_UPLOADED.getMessage();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void uploadImageOnReplica(final UUID identityUUID, final Image image) {
        Identity identityReplica = this.identityReplicaRepository.findById(identityUUID)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));
        image.setPreview(true);
        this.imageReplicaRepository.save(image);
        identityReplica.setImage(image);
        this.identityReplicaRepository.save(identityReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String changeImageOnMaster(final UUID identityId, final MultipartFile multipartFile) throws IOException {
        UUID identityUUID = identityId == null ? UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId;

        Identity identityMaster = this.identityMasterRepository.findById(identityUUID)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        if (identityMaster.getImage() != null) {
            Image image = this.imageService.changeOnMaster(identityMaster.getImage().getId(), multipartFile);

            image.setPreview(true);
            this.imageMasterRepository.save(image);

            identityMaster.setImage(image);
            this.identityMasterRepository.save(identityMaster);

            this.beanFactory.getBean(IdentityService.class).changeImageOnReplica(identityUUID, image);

            return IdentityMessages.IDENTITY_IMAGE_CHANGED.getMessage();

        } else {
            throw new ImageNotFoundException(IdentityMessages.IDENTITY_IMAGE_NOT_FOUND.getMessage());
        }
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void changeImageOnReplica(final UUID identityUUID, final Image image) {
        Identity identityReplica = this.identityReplicaRepository.findById(identityUUID)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        if (identityReplica.getImage() != null) {
            image.setPreview(true);
            this.imageReplicaRepository.save(image);
            identityReplica.setImage(image);
            this.identityReplicaRepository.save(identityReplica);

        } else {
            throw new ImageNotFoundException(IdentityMessages.IDENTITY_IMAGE_NOT_FOUND.getMessage());
        }
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String deleteImageFromMaster(final UUID identityId) {
        UUID identityUUID = identityId == null ? UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId;

        Identity identityMaster = this.identityMasterRepository.findById(identityUUID)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        if (identityMaster.getImage() != null) {

            UUID identityMasterId = identityMaster.getImage().getId();
            identityMaster.setImage(null);
            this.identityMasterRepository.save(identityMaster);
            this.imageMasterRepository.deleteById(identityMasterId);

            this.beanFactory.getBean(IdentityService.class).deleteImageFromReplica(identityUUID);

            return IdentityMessages.IDENTITY_IMAGE_DELETED.getMessage();

        } else {
            throw new ImageNotFoundException(IdentityMessages.IDENTITY_IMAGE_NOT_FOUND.getMessage());
        }
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void deleteImageFromReplica(final UUID identityUUID) {
        Identity identityReplica = this.identityReplicaRepository.findById(identityUUID)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        if (identityReplica.getImage() != null) {
            UUID identityReplicaId = identityReplica.getImage().getId();
            identityReplica.setImage(null);

            this.identityReplicaRepository.save(identityReplica);
            this.imageReplicaRepository.deleteById(identityReplicaId);

        } else {
            throw new ImageNotFoundException(IdentityMessages.IDENTITY_IMAGE_NOT_FOUND.getMessage());
        }
    }
}
