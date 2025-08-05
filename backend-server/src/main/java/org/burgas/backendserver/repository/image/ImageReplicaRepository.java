package org.burgas.backendserver.repository.image;

import org.burgas.backendserver.entity.Image;
import org.burgas.backendserver.repository.ReplicaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@ReplicaRepository
public interface ImageReplicaRepository extends JpaRepository<Image, UUID> {
}
