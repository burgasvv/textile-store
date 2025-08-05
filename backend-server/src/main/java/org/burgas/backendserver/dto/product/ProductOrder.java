package org.burgas.backendserver.dto.product;

import lombok.*;
import org.burgas.backendserver.dto.Response;
import org.burgas.backendserver.dto.category.CategoryWithoutProducts;
import org.burgas.backendserver.entity.Image;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrder extends Response {

    private UUID id;
    private CategoryWithoutProducts category;
    private String name;
    private String description;
    private Double price;
    private Long amount;
    private List<Image> images;
}
