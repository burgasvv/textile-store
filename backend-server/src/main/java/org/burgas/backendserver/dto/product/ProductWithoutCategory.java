package org.burgas.backendserver.dto.product;

import lombok.*;
import org.burgas.backendserver.dto.Response;
import org.burgas.backendserver.entity.Image;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithoutCategory extends Response {

    private UUID id;
    private String name;
    private String description;
    private Double price;
    private List<Image> images;
}
