package org.burgas.backendserver.mapper;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.bill.BillResponse;
import org.burgas.backendserver.entity.Bill;
import org.burgas.backendserver.repository.product.ProductReplicaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public final class BillMapper {

    private final IdentityMapper identityMapper;
    private final ProductReplicaRepository productReplicaRepository;
    private final ProductMapper productMapper;

    public BillResponse toBillResponse(final Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .cost(bill.getCost())
                .identity(
                        Optional.ofNullable(bill.getIdentity())
                                .map(this.identityMapper::toResponse)
                                .orElse(null)
                )
                .products(
                        this.productReplicaRepository.findProductInBill(bill.getId())
                                .stream()
                                .map(product -> this.productMapper.toProductOrderInBill(bill.getId(), product))
                                .toList()
                )
                .build();
    }
}
