package org.burgas.backendserver.service;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.category.CategoryRequest;
import org.burgas.backendserver.dto.category.CategoryWithProducts;
import org.burgas.backendserver.dto.category.CategoryWithoutProducts;
import org.burgas.backendserver.entity.Category;
import org.burgas.backendserver.exception.CategoryNotFoundException;
import org.burgas.backendserver.exception.IdEmptyException;
import org.burgas.backendserver.mapper.CategoryMapper;
import org.burgas.backendserver.message.CategoryMessages;
import org.burgas.backendserver.repository.category.CategoryMasterRepository;
import org.burgas.backendserver.repository.category.CategoryReplicaRepository;
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
public class CategoryService {

    private final CategoryMasterRepository categoryMasterRepository;
    private final CategoryReplicaRepository categoryReplicaRepository;
    private final CategoryMapper categoryMapper;
    private final BeanFactory beanFactory;

    public List<CategoryWithoutProducts> findAll() {
        return this.categoryReplicaRepository.findAll()
                .stream()
                .map(this.categoryMapper::toCategoryWithoutProducts)
                .collect(Collectors.toList());
    }

    public CategoryWithProducts findById(final UUID categoryId) {
        return this.categoryReplicaRepository.findById(categoryId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : categoryId)
                .map(this.categoryMapper::toCategoryWithProducts)
                .orElseThrow(
                        () -> new CategoryNotFoundException(CategoryMessages.CATEGORY_NOT_FOUND.getMessage())
                );
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public CategoryWithProducts create(final CategoryRequest categoryRequest) {
        UUID categoryId = UUID.randomUUID();
        while (this.categoryMasterRepository.existsById(categoryId) && this.categoryReplicaRepository.existsById(categoryId))
            categoryId = UUID.randomUUID();

        Category categoryMaster = this.categoryMapper.toEntityMaster(categoryRequest);
        categoryMaster.setId(categoryId);
        categoryMaster = this.categoryMasterRepository.save(categoryMaster);

        this.beanFactory.getBean(CategoryService.class).createOnReplica(categoryId, categoryRequest);

        return this.categoryMapper.toCategoryWithProducts(categoryMaster);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void createOnReplica(final UUID categoryId, final CategoryRequest categoryRequest) {
        Category categoryReplica = this.categoryMapper.toEntityReplica(categoryRequest);
        categoryReplica.setId(categoryId);
        this.categoryReplicaRepository.save(categoryReplica);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String createMultiple(final List<CategoryRequest> categoryRequests) {
        categoryRequests.forEach(this::create);
        return CategoryMessages.MULTIPLE_CATEGORIES_CREATED.getMessage();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public CategoryWithProducts updateOnMaster(final UUID categoryId, final CategoryRequest categoryRequest) {
        if (categoryId == null)
            throw new IdEmptyException(CategoryMessages.CATEGORY_ID_EMPTY.getMessage());

        categoryRequest.setId(categoryId);
        Category categoryMaster = this.categoryMasterRepository.save(this.categoryMapper.toEntityMaster(categoryRequest));
        this.beanFactory.getBean(CategoryService.class).updateOnReplica(categoryRequest);

        return this.categoryMapper.toCategoryWithProducts(categoryMaster);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void updateOnReplica(final CategoryRequest categoryRequest) {
        this.categoryReplicaRepository.save(this.categoryMapper.toEntityReplica(categoryRequest));
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public String deleteFromMaster(final UUID categoryId) {
        if (categoryId == null)
            throw new IdEmptyException(CategoryMessages.CATEGORY_ID_EMPTY.getMessage());

        Category categoryMaster = this.categoryMasterRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(CategoryMessages.CATEGORY_NOT_FOUND.getMessage()));
        this.categoryMasterRepository.delete(categoryMaster);

        this.beanFactory.getBean(CategoryService.class).deleteFromReplica(categoryId);

        return CategoryMessages.CATEGORY_DELETED.getMessage();
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void deleteFromReplica(final UUID categoryId) {
        Category categoryReplica = this.categoryReplicaRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(CategoryMessages.CATEGORY_NOT_FOUND.getMessage()));
        this.categoryReplicaRepository.delete(categoryReplica);
    }
}
