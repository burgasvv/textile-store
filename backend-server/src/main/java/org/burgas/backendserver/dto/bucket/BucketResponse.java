package org.burgas.backendserver.dto.bucket;

import lombok.*;
import org.burgas.backendserver.dto.Response;
import org.burgas.backendserver.dto.identity.IdentityResponse;
import org.burgas.backendserver.dto.product.ProductOrder;
import org.burgas.backendserver.dto.product.ProductWithCategory;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BucketResponse extends Response {

    private UUID id;
    private Double cost;
    private IdentityResponse identity;
    private List<ProductOrder> products;
}
