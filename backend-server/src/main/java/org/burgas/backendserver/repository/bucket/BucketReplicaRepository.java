package org.burgas.backendserver.repository.bucket;

import org.burgas.backendserver.entity.Bucket;
import org.burgas.backendserver.repository.ReplicaRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@ReplicaRepository
public interface BucketReplicaRepository extends JpaRepository<Bucket, UUID> {

    Optional<Bucket> findBucketByIdentityId(UUID identityId);
}
