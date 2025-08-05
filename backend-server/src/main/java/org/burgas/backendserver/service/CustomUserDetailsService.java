package org.burgas.backendserver.service;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.exception.IdentityNotFoundException;
import org.burgas.backendserver.message.IdentityMessages;
import org.burgas.backendserver.repository.identity.IdentityReplicaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS, transactionManager = "replicaPostgresTransactionManager")
public class CustomUserDetailsService implements UserDetailsService {

    private final IdentityReplicaRepository identityReplicaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.identityReplicaRepository.findIdentityByEmail(username)
                .orElseThrow(
                        () -> new IdentityNotFoundException(IdentityMessages.IDENTITY_FIELD_EMAIL_EMPTY.getMessage())
                );
    }
}
