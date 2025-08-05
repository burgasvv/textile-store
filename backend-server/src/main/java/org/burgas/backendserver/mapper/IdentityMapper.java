package org.burgas.backendserver.mapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.identity.IdentityRequest;
import org.burgas.backendserver.dto.identity.IdentityResponse;
import org.burgas.backendserver.entity.Identity;
import org.burgas.backendserver.repository.identity.IdentityMasterRepository;
import org.burgas.backendserver.repository.identity.IdentityReplicaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.burgas.backendserver.entity.Authority.USER;
import static org.burgas.backendserver.message.IdentityMessages.*;

@Component
@RequiredArgsConstructor
public final class IdentityMapper implements EntityMapper<IdentityRequest, Identity, IdentityResponse> {

    private final IdentityMasterRepository identityMasterRepository;
    private final IdentityReplicaRepository identityReplicaRepository;

    @Getter
    private final PasswordEncoder passwordEncoder;

    @Override
    public Identity toEntityMaster(IdentityRequest identityRequest) {
        UUID identityId = this.handleData(identityRequest.getId(), UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)));
        String password = identityRequest.getPassword() == null || identityRequest.getPassword().isBlank() ? "" : identityRequest.getPassword();
        return this.identityMasterRepository.findById(identityId)
                .map(
                        identity -> Identity.builder()
                                .id(identity.getId())
                                .username(this.handleData(identityRequest.getUsername(), identity.getNonUserDetailsUsername()))
                                .password(identity.getPassword())
                                .email(this.handleData(identityRequest.getEmail(), identity.getEmail()))
                                .phone(this.handleData(identityRequest.getPhone(), identity.getPhone()))
                                .authority(USER)
                                .enabled(true)
                                .image(null)
                                .build()
                )
                .orElseGet(
                        () -> {
                            String newPassword = this.handleDataThrowable(password, IDENTITY_FIELD_PASSWORD_EMPTY.getMessage());
                            return Identity.builder()
                                    .username(this.handleDataThrowable(identityRequest.getUsername(), IDENTITY_FIELD_USERNAME_EMPTY.getMessage()))
                                    .password(this.passwordEncoder.encode(newPassword))
                                    .email(this.handleDataThrowable(identityRequest.getEmail(), IDENTITY_FIELD_EMAIL_EMPTY.getMessage()))
                                    .phone(this.handleDataThrowable(identityRequest.getPhone(), IDENTITY_FIELD_PHONE_EMPTY.getMessage()))
                                    .authority(USER)
                                    .enabled(true)
                                    .image(null)
                                    .build();
                        }
                );
    }

    @Override
    public Identity toEntityReplica(IdentityRequest identityRequest) {
        UUID identityId = this.handleData(identityRequest.getId(), UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)));
        String password = identityRequest.getPassword() == null || identityRequest.getPassword().isBlank() ? "" : identityRequest.getPassword();
        return this.identityReplicaRepository.findById(identityId)
                .map(
                        identity -> Identity.builder()
                                .id(identity.getId())
                                .username(this.handleData(identityRequest.getUsername(), identity.getNonUserDetailsUsername()))
                                .password(identity.getPassword())
                                .email(this.handleData(identityRequest.getEmail(), identity.getEmail()))
                                .phone(this.handleData(identityRequest.getPhone(), identity.getPhone()))
                                .authority(USER)
                                .enabled(true)
                                .image(null)
                                .build()
                )
                .orElseGet(
                        () -> {
                            String newPassword = this.handleDataThrowable(password, IDENTITY_FIELD_PASSWORD_EMPTY.getMessage());
                            return Identity.builder()
                                    .username(this.handleDataThrowable(identityRequest.getUsername(), IDENTITY_FIELD_USERNAME_EMPTY.getMessage()))
                                    .password(this.passwordEncoder.encode(newPassword))
                                    .email(this.handleDataThrowable(identityRequest.getEmail(), IDENTITY_FIELD_EMAIL_EMPTY.getMessage()))
                                    .phone(this.handleDataThrowable(identityRequest.getPhone(), IDENTITY_FIELD_PHONE_EMPTY.getMessage()))
                                    .authority(USER)
                                    .enabled(true)
                                    .image(null)
                                    .build();
                        }
                );
    }

    @Override
    public IdentityResponse toResponse(Identity identity) {
        return IdentityResponse.builder()
                .id(identity.getId())
                .username(identity.getNonUserDetailsUsername())
                .password(identity.getPassword())
                .email(identity.getEmail())
                .phone(identity.getPhone())
                .enabled(identity.getEnabled())
                .authority(identity.getAuthority())
                .image(identity.getImage())
                .build();
    }
}
