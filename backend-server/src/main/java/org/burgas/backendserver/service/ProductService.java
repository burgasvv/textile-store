package org.burgas.backendserver.service;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.product.ProductRequest;
import org.burgas.backendserver.dto.product.ProductWithCategory;
import org.burgas.backendserver.dto.product.ProductWithoutCategory;
import org.burgas.backendserver.entity.Product;
import org.burgas.backendserver.exception.IdEmptyException;
import org.burgas.backendserver.exception.ProductNotFoundException;
import org.burgas.backendserver.mapper.ProductMapper;
import org.burgas.backendserver.message.ProductMessages;
import org.burgas.backendserver.repository.product.ProductMasterRepository;
import org.burgas.backendserver.repository.product.ProductReplicaRepository;
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
public class ProductService {

    private final ProductMasterRepository productMasterRepository;
    private final ProductReplicaRepository productReplicaRepository;
    private final ProductMapper productMapper;
    private final BeanFactory beanFactory;

    public List<ProductWithoutCategory> findAll() {
        return this.productReplicaRepository.findAll()
                .stream()
                .map(this.productMapper::toProductWithoutCategory)
                .collect(Collectors.toList());
    }

    public ProductWithCategory findById(final UUID productId) {
        return this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .map(this.productMapper::toProductWithCategory)
                .orElseThrow(
                        () -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage())
                );
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public ProductWithCategory createOnMaster(final ProductRequest productRequest) {
        UUID productId = UUID.randomUUID();
        while (this.productMasterRepository.existsById(productId) && this.productReplicaRepository.existsById(productId))
            productId = UUID.randomUUID();

        Product productMaster = this.productMapper.toEntityMaster(productRequest);
        productMaster.setId(productId);
        productMaster = this.productMasterRepository.save(productMaster);

        this.beanFactory.getBean(ProductService.class).createOnReplica(productId, productRequest);

        return this.productMapper.toProductWithCategory(productMaster);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void createOnReplica(final UUID productId, final ProductRequest productRequest) {
        Product productReplica = this.productMapper.toEntityReplica(productRequest);
        productReplica.setId(productId);
        this.productReplicaRepository.save(productReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public ProductWithCategory updateOnMaster(final UUID productId, final ProductRequest productRequest) {
        if (productId == null)
            throw new IdEmptyException(ProductMessages.PRODUCT_ID_EMPTY.getMessage());

        productRequest.setId(productId);

        Product productMaster = this.productMasterRepository.save(this.productMapper.toEntityMaster(productRequest));
        this.beanFactory.getBean(ProductService.class).updateOnReplica(productRequest);

        return this.productMapper.toProductWithCategory(productMaster);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void updateOnReplica(final ProductRequest productRequest) {
        this.productReplicaRepository.save(this.productMapper.toEntityReplica(productRequest));
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String deleteFromMaster(final UUID productId) {
        if (productId == null)
            throw new IdEmptyException(ProductMessages.PRODUCT_ID_EMPTY.getMessage());

        Product productMaster = this.productMasterRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        this.productMasterRepository.delete(productMaster);
        this.beanFactory.getBean(ProductService.class).deleteFromReplica(productId);

        return ProductMessages.PRODUCT_DELETED.getMessage();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void deleteFromReplica(final UUID productId) {
        Product productReplica = this.productReplicaRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));
        this.productReplicaRepository.delete(productReplica);
    }
}
