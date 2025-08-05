package org.burgas.backendserver.mapper;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.category.CategoryRequest;
import org.burgas.backendserver.dto.category.CategoryWithProducts;
import org.burgas.backendserver.dto.category.CategoryWithoutProducts;
import org.burgas.backendserver.entity.Category;
import org.burgas.backendserver.exception.EntityFieldEmptyException;
import org.burgas.backendserver.message.CategoryMessages;
import org.burgas.backendserver.repository.category.CategoryMasterRepository;
import org.burgas.backendserver.repository.category.CategoryReplicaRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class CategoryMapper {

    private final CategoryMasterRepository categoryMasterRepository;
    private final CategoryReplicaRepository categoryReplicaRepository;
    private final ProductMapper productMapper;

    private  <D> D handleData(D requestData, D entityData) {
        return requestData == null || requestData == "" ? entityData : requestData;
    }

    private  <D> D handleDataThrowable(D requestData, String message) {
        if (requestData == null || requestData == "")
            throw new EntityFieldEmptyException(message);
        return requestData;
    }

    public Category toEntityMaster(final CategoryRequest categoryRequest) {
        UUID categoryId = this.handleData(categoryRequest.getId(), UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)));
        return this.categoryMasterRepository.findById(categoryId)
                .map(
                        category -> Category.builder()
                                .id(category.getId())
                                .name(this.handleData(categoryRequest.getName(), category.getName()))
                                .build()
                )
                .orElseGet(
                        () -> Category.builder()
                                .name(this.handleDataThrowable(categoryRequest.getName(), CategoryMessages.CATEGORY_FIELD_NAME_EMPTY.getMessage()))
                                .build()
                );
    }

    public Category toEntityReplica(final CategoryRequest categoryRequest) {
        UUID categoryId = this.handleData(categoryRequest.getId(), UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)));
        return this.categoryReplicaRepository.findById(categoryId)
                .map(
                        category -> Category.builder()
                                .id(category.getId())
                                .name(this.handleData(categoryRequest.getName(), category.getName()))
                                .build()
                )
                .orElseGet(
                        () -> Category.builder()
                                .name(this.handleDataThrowable(categoryRequest.getName(), CategoryMessages.CATEGORY_FIELD_NAME_EMPTY.getMessage()))
                                .build()
                );
    }

    public CategoryWithoutProducts toCategoryWithoutProducts(final Category category) {
        return CategoryWithoutProducts.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public CategoryWithProducts toCategoryWithProducts(final Category category) {
        return CategoryWithProducts.builder()
                .id(category.getId())
                .name(category.getName())
                .products(
                        category.getProducts() == null ? null : category.getProducts()
                                .stream()
                                .map(this.productMapper::toProductWithoutCategory)
                                .toList()
                )
                .build();
    }
}
