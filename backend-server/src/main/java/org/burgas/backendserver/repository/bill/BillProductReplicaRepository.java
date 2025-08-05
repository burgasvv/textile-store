package org.burgas.backendserver.repository.bill;

import org.burgas.backendserver.entity.BillProduct;
import org.burgas.backendserver.entity.BillProductPK;
import org.burgas.backendserver.repository.ReplicaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@ReplicaRepository
public interface BillProductReplicaRepository extends JpaRepository<BillProduct, BillProductPK> {

    Optional<BillProduct> findBillProductByBillIdAndProductId(UUID billId, UUID productId);
}
