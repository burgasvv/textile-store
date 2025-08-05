package org.burgas.backendserver.mapper;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.bucket.BucketResponse;
import org.burgas.backendserver.entity.Bucket;
import org.burgas.backendserver.repository.product.ProductReplicaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public final class BucketMapper {

    private final IdentityMapper identityMapper;
    private final ProductReplicaRepository productReplicaRepository;
    private final ProductMapper productMapper;

    public BucketResponse toBucketResponse(final Bucket bucket) {
        return BucketResponse.builder()
                .id(bucket.getId())
                .cost(bucket.getCost())
                .identity(
                        Optional.ofNullable(bucket.getIdentity())
                                .map(this.identityMapper::toResponse)
                                .orElse(null)
                )
                .products(
                        this.productReplicaRepository.findProductsInBucket(bucket.getId())
                                .stream()
                                .map(product -> this.productMapper.toProductOrderInBucket(bucket.getId(), product))
                                .toList()
                )
                .build();
    }
}
