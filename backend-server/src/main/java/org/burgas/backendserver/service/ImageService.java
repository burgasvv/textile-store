package org.burgas.backendserver.service;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.entity.Image;
import org.burgas.backendserver.exception.ImageNotFoundException;
import org.burgas.backendserver.exception.MultipartFileEmptyException;
import org.burgas.backendserver.message.ImageMessages;
import org.burgas.backendserver.repository.image.ImageMasterRepository;
import org.burgas.backendserver.repository.image.ImageReplicaRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS, transactionManager = "replicaPostgresTransactionManager")
public class ImageService {

    private final ImageMasterRepository imageMasterRepository;
    private final ImageReplicaRepository imageReplicaRepository;
    private final BeanFactory beanFactory;

    public Image findById(final UUID imageId) {
        return this.imageReplicaRepository.findById(imageId == null ? UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : imageId)
                .orElseThrow(() -> new ImageNotFoundException(ImageMessages.IMAGE_NOT_FOUND.getMessage()));
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public Image uploadToMaster(final MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty())
            throw new MultipartFileEmptyException(ImageMessages.MULTIPART_FILE_EMPTY.getMessage());

        UUID imageId = UUID.randomUUID();

        while (this.imageMasterRepository.existsById(imageId) && this.imageReplicaRepository.existsById(imageId))
            imageId = UUID.randomUUID();

        Image image = Image.builder()
                .id(imageId)
                .name(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .format(Objects.requireNonNull(multipartFile.getContentType()).split("/")[1])
                .size(multipartFile.getSize())
                .data(multipartFile.getBytes())
                .preview(false)
                .build();

        Image imageMaster = this.imageMasterRepository.save(image);
        this.beanFactory.getBean(ImageService.class).uploadToReplica(image);

        return imageMaster;
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void uploadToReplica(final Image image) {
        this.imageReplicaRepository.save(image);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public Image changeOnMaster(final UUID imageId, final MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty())
            throw new MultipartFileEmptyException(ImageMessages.MULTIPART_FILE_EMPTY.getMessage());

        UUID id = imageId == null ? UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : imageId;
        Image image = Image.builder()
                .id(id)
                .name(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .format(Objects.requireNonNull(multipartFile.getContentType()).split("/")[1])
                .size(multipartFile.getSize())
                .data(multipartFile.getBytes())
                .preview(false)
                .build();

        Image imageMaster = this.imageMasterRepository.findById(id)
                .map(img -> image)
                .orElseThrow(() -> new ImageNotFoundException(ImageMessages.IMAGE_NOT_FOUND.getMessage()));
        imageMaster = this.imageMasterRepository.save(imageMaster);

        this.beanFactory.getBean(ImageService.class).changeOnReplica(id, image);

        return imageMaster;
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void changeOnReplica(final UUID id, final Image image) {
        Image imageReplica = this.imageReplicaRepository.findById(id)
                .map(img -> image)
                .orElseThrow(() -> new ImageNotFoundException(ImageMessages.IMAGE_NOT_FOUND.getMessage()));
        this.imageReplicaRepository.save(imageReplica);
    }

    @SuppressWarnings("unused")
    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public void deleteFromMaster(final UUID imageId) {
        UUID id = imageId == null ? UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : imageId;

        Image imageMaster = this.imageMasterRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException(ImageMessages.IMAGE_NOT_FOUND.getMessage()));
        this.imageMasterRepository.delete(imageMaster);

        this.beanFactory.getBean(ImageService.class).deleteFromReplica(id);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void deleteFromReplica(final UUID id) {
        Image imageReplica = this.imageReplicaRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException(ImageMessages.IMAGE_NOT_FOUND.getMessage()));
        this.imageReplicaRepository.delete(imageReplica);
    }
}
