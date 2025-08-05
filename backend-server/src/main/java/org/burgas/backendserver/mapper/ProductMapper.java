package org.burgas.backendserver.mapper;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.category.CategoryWithoutProducts;
import org.burgas.backendserver.dto.product.ProductOrder;
import org.burgas.backendserver.dto.product.ProductRequest;
import org.burgas.backendserver.dto.product.ProductWithCategory;
import org.burgas.backendserver.dto.product.ProductWithoutCategory;
import org.burgas.backendserver.entity.BillProduct;
import org.burgas.backendserver.entity.BucketProduct;
import org.burgas.backendserver.entity.Product;
import org.burgas.backendserver.exception.BillProductNotFoundException;
import org.burgas.backendserver.exception.BucketProductNotFoundException;
import org.burgas.backendserver.exception.EntityFieldEmptyException;
import org.burgas.backendserver.message.BillMessages;
import org.burgas.backendserver.message.BucketMessages;
import org.burgas.backendserver.message.ProductMessages;
import org.burgas.backendserver.repository.bill.BillProductReplicaRepository;
import org.burgas.backendserver.repository.bucket.BucketProductReplicaRepository;
import org.burgas.backendserver.repository.category.CategoryMasterRepository;
import org.burgas.backendserver.repository.category.CategoryReplicaRepository;
import org.burgas.backendserver.repository.product.ProductMasterRepository;
import org.burgas.backendserver.repository.product.ProductReplicaRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class ProductMapper {

    private final ProductMasterRepository productMasterRepository;
    private final ProductReplicaRepository productReplicaRepository;

    private final CategoryMasterRepository categoryMasterRepository;
    private final CategoryReplicaRepository categoryReplicaRepository;

    private final BucketProductReplicaRepository bucketProductReplicaRepository;
    private final BillProductReplicaRepository billProductReplicaRepository;

    private  <D> D handleData(D requestData, D entityData) {
        return requestData == null || requestData == "" ? entityData : requestData;
    }

    private  <D> D handleDataThrowable(D requestData, String message) {
        if (requestData == null || requestData == "")
            throw new EntityFieldEmptyException(message);
        return requestData;
    }

    public Product toEntityMaster(final ProductRequest productRequest) {
        UUID productId = this.handleData(productRequest.getId(), UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)));
        return this.productMasterRepository.findById(productId)
                .map(
                        product -> Product.builder()
                                .id(product.getId())
                                .name(this.handleData(productRequest.getName(), product.getName()))
                                .description(this.handleData(productRequest.getDescription(), product.getDescription()))
                                .price(this.handleData(productRequest.getPrice(), product.getPrice()))
                                .category(
                                        this.handleData(
                                                this.categoryMasterRepository.findById(
                                                        productRequest.getCategoryId() == null ?
                                                                UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) :
                                                                productRequest.getCategoryId()
                                                )
                                                        .orElse(null),

                                                product.getCategory()
                                        )
                                )
                                .build()
                )
                .orElseGet(
                        () -> Product.builder()
                                .name(this.handleDataThrowable(productRequest.getName(), ProductMessages.PRODUCT_ENTITY_FIELD_NAME_EMPTY.getMessage()))
                                .description(this.handleDataThrowable(productRequest.getDescription(), ProductMessages.PRODUCT_ENTITY_FIELD_DESCRIPTION_EMPTY.getMessage()))
                                .price(this.handleDataThrowable(productRequest.getPrice(), ProductMessages.PRODUCT_ENTITY_FIELD_PRICE_EMPTY.getMessage()))
                                .category(
                                        this.handleDataThrowable(
                                                this.categoryMasterRepository.findById(
                                                                productRequest.getCategoryId() == null ?
                                                                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) :
                                                                        productRequest.getCategoryId()
                                                        )
                                                        .orElse(null),

                                                ProductMessages.PRODUCT_ENTITY_FIELD_CATEGORY_EMPTY.getMessage()
                                        )
                                )
                                .build()
                );
    }

    public Product toEntityReplica(final ProductRequest productRequest) {
        UUID productId = this.handleData(productRequest.getId(), UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)));
        return this.productReplicaRepository.findById(productId)
                .map(
                        product -> Product.builder()
                                .id(product.getId())
                                .name(this.handleData(productRequest.getName(), product.getName()))
                                .description(this.handleData(productRequest.getDescription(), product.getDescription()))
                                .price(this.handleData(productRequest.getPrice(), product.getPrice()))
                                .category(
                                        this.handleData(
                                                this.categoryReplicaRepository.findById(
                                                                productRequest.getCategoryId() == null ?
                                                                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) :
                                                                        productRequest.getCategoryId()
                                                        )
                                                        .orElse(null),

                                                product.getCategory()
                                        )
                                )
                                .build()
                )
                .orElseGet(
                        () -> Product.builder()
                                .name(this.handleDataThrowable(productRequest.getName(), ProductMessages.PRODUCT_ENTITY_FIELD_NAME_EMPTY.getMessage()))
                                .description(this.handleDataThrowable(productRequest.getDescription(), ProductMessages.PRODUCT_ENTITY_FIELD_DESCRIPTION_EMPTY.getMessage()))
                                .price(this.handleDataThrowable(productRequest.getPrice(), ProductMessages.PRODUCT_ENTITY_FIELD_PRICE_EMPTY.getMessage()))
                                .category(
                                        this.handleDataThrowable(
                                                this.categoryReplicaRepository.findById(
                                                                productRequest.getCategoryId() == null ?
                                                                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) :
                                                                        productRequest.getCategoryId()
                                                        )
                                                        .orElse(null),

                                                ProductMessages.PRODUCT_ENTITY_FIELD_CATEGORY_EMPTY.getMessage()
                                        )
                                )
                                .build()
                );
    }

    public ProductWithoutCategory toProductWithoutCategory(final Product product) {
        return ProductWithoutCategory.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .images(product.getImages())
                .build();
    }

    public ProductWithCategory toProductWithCategory(final Product product) {
        return ProductWithCategory.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(
                        Optional.ofNullable(product.getCategory())
                                .map(
                                        category -> CategoryWithoutProducts.builder()
                                                .id(category.getId())
                                                .name(category.getName())
                                                .build()
                                )
                                .orElse(null)
                )
                .images(product.getImages())
                .build();
    }

    public ProductOrder toProductOrderInBucket(final UUID bucketId, final Product product) {
        BucketProduct bucketProduct = this.bucketProductReplicaRepository.findBucketProductByBucketIdAndProductId(bucketId, product.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        return ProductOrder.builder()
                .id(product.getId())
                .category(
                        Optional.ofNullable(product.getCategory())
                                .map(
                                        category -> CategoryWithoutProducts.builder()
                                                .id(category.getId())
                                                .name(category.getName())
                                                .build()
                                )
                                .orElse(null)
                )
                .name(product.getName())
                .description(product.getDescription())
                .price(bucketProduct.getCost())
                .amount(bucketProduct.getAmount())
                .images(product.getImages())
                .build();
    }

    public ProductOrder toProductOrderInBill(final UUID billId, final Product product) {
        BillProduct billProduct = this.billProductReplicaRepository.findBillProductByBillIdAndProductId(billId, product.getId())
                .orElseThrow(() -> new BillProductNotFoundException(BillMessages.BILL_PRODUCT_NOT_FOUND.getMessage()));

        return ProductOrder.builder()
                .id(product.getId())
                .category(
                        Optional.ofNullable(product.getCategory())
                                .map(
                                        category -> CategoryWithoutProducts.builder()
                                                .id(category.getId())
                                                .name(category.getName())
                                                .build()
                                )
                                .orElse(null)
                )
                .name(product.getName())
                .description(product.getDescription())
                .price(billProduct.getCost())
                .amount(billProduct.getAmount())
                .images(product.getImages())
                .build();
    }
}
