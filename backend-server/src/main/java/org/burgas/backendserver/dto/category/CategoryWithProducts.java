package org.burgas.backendserver.dto.category;

import lombok.*;
import org.burgas.backendserver.dto.Response;
import org.burgas.backendserver.dto.product.ProductWithoutCategory;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithProducts extends Response {

    private UUID id;
    private String name;
    private List<ProductWithoutCategory> products;
}
