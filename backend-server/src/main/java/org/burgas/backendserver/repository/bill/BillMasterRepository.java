package org.burgas.backendserver.repository.bill;

import org.burgas.backendserver.entity.Bill;
import org.burgas.backendserver.repository.MasterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@MasterRepository
public interface BillMasterRepository extends JpaRepository<Bill, UUID> {
}
