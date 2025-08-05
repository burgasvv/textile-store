package org.burgas.backendserver.repository.bucket;

import org.burgas.backendserver.entity.BucketProduct;
import org.burgas.backendserver.entity.BucketProductPK;
import org.burgas.backendserver.repository.MasterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@MasterRepository
public interface BucketProductMasterRepository extends JpaRepository<BucketProduct, BucketProductPK> {

    Optional<BucketProduct> findBucketProductByBucketIdAndProductId(UUID bucketId, UUID productId);

    void deleteBucketProductsByBucketId(UUID bucketId);
}
