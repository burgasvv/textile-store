package org.burgas.backendserver.repository.bill;

import org.burgas.backendserver.entity.BillProduct;
import org.burgas.backendserver.entity.BillProductPK;
import org.burgas.backendserver.repository.MasterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@MasterRepository
public interface BillProductMasterRepository extends JpaRepository<BillProduct, BillProductPK> {
}
