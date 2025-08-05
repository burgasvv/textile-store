package org.burgas.backendserver.repository.identity;

import org.burgas.backendserver.entity.Identity;
import org.burgas.backendserver.repository.MasterRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MasterRepository
public interface IdentityMasterRepository extends JpaRepository<Identity, UUID> {

    @Override
    @EntityGraph(value = "identity-with-image")
    @NotNull List<Identity> findAll();

    @Override
    @EntityGraph(value = "identity-with-image")
    @NotNull Optional<Identity> findById(@NotNull UUID uuid);
}
