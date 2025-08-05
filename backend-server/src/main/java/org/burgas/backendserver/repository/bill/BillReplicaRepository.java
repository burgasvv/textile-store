package org.burgas.backendserver.repository.bill;

import org.burgas.backendserver.entity.Bill;
import org.burgas.backendserver.repository.ReplicaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@ReplicaRepository
public interface BillReplicaRepository extends JpaRepository<Bill, UUID> {

    List<Bill> findBillsByIdentityId(UUID identityId);
}
