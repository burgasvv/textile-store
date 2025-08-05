package org.burgas.backendserver.repository.bucket;

import org.burgas.backendserver.entity.Bucket;
import org.burgas.backendserver.repository.MasterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@MasterRepository
public interface BucketMasterRepository extends JpaRepository<Bucket, UUID> {
}
