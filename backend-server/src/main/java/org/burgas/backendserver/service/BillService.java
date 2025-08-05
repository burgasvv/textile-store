package org.burgas.backendserver.service;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.bill.BillResponse;
import org.burgas.backendserver.dto.bucket.BucketResponse;
import org.burgas.backendserver.entity.Bill;
import org.burgas.backendserver.entity.BillProduct;
import org.burgas.backendserver.exception.BillNotFoundException;
import org.burgas.backendserver.mapper.BillMapper;
import org.burgas.backendserver.message.BillMessages;
import org.burgas.backendserver.repository.bill.BillMasterRepository;
import org.burgas.backendserver.repository.bill.BillProductMasterRepository;
import org.burgas.backendserver.repository.bill.BillProductReplicaRepository;
import org.burgas.backendserver.repository.bill.BillReplicaRepository;
import org.burgas.backendserver.repository.identity.IdentityMasterRepository;
import org.burgas.backendserver.repository.identity.IdentityReplicaRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS, transactionManager = "replicaPostgresTransactionManager")
public class BillService {

    private final BillMasterRepository billMasterRepository;
    private final BillReplicaRepository billReplicaRepository;

    private final BillMapper billMapper;

    private final BillProductMasterRepository billProductMasterRepository;
    private final BillProductReplicaRepository billProductReplicaRepository;

    private final IdentityMasterRepository identityMasterRepository;
    private final IdentityReplicaRepository identityReplicaRepository;

    private final BeanFactory beanFactory;

    public List<BillResponse> findByIdentityId(final UUID identityId) {
        return this.billReplicaRepository.findBillsByIdentityId(identityId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId)
                .stream()
                .map(this.billMapper::toBillResponse)
                .collect(Collectors.toList());
    }

    public BillResponse findById(final UUID billId) {
        return this.billReplicaRepository.findById(billId == null ? UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : billId)
                .map(this.billMapper::toBillResponse)
                .orElseThrow(() -> new BillNotFoundException(BillMessages.BILL_NOT_FOUND.getMessage()));
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BillResponse createBillOnMaster(final BucketResponse bucketResponse) {
        UUID billId = UUID.randomUUID();
        while (this.billMasterRepository.existsById(billId) && this.billReplicaRepository.existsById(billId))
            billId = UUID.randomUUID();

        Bill bill = Bill.builder()
                .id(billId)
                .cost(bucketResponse.getCost())
                .identity(this.identityMasterRepository.findById(bucketResponse.getIdentity().getId()).orElse(null))
                .build();

        bill = this.billMasterRepository.save(bill);
        Bill finalBill = bill;
        bucketResponse.getProducts().forEach(
                productOrder -> this.billProductMasterRepository.save(
                        BillProduct.builder()
                                .billId(finalBill.getId())
                                .productId(productOrder.getId())
                                .amount(productOrder.getAmount())
                                .cost(productOrder.getPrice())
                                .build()
                )
        );
        this.beanFactory.getBean(BillService.class).createBillOnReplica(billId, bucketResponse);
        return this.billMapper.toBillResponse(bill);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void createBillOnReplica(final UUID billId, final BucketResponse bucketResponse) {
        Bill bill = Bill.builder()
                .id(billId)
                .cost(bucketResponse.getCost())
                .identity(this.identityReplicaRepository.findById(bucketResponse.getIdentity().getId()).orElse(null))
                .build();

        bill = this.billReplicaRepository.save(bill);
        Bill finalBill = bill;
        bucketResponse.getProducts().forEach(
                productOrder -> this.billProductReplicaRepository.save(
                        BillProduct.builder()
                                .billId(finalBill.getId())
                                .productId(productOrder.getId())
                                .amount(productOrder.getAmount())
                                .cost(productOrder.getPrice())
                                .build()
                )
        );
    }
}
