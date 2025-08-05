package org.burgas.backendserver.dto.product;

import lombok.*;
import org.burgas.backendserver.dto.Request;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest extends Request {

    private UUID id;
    private String name;
    private String description;
    private Double price;
    private UUID categoryId;
}
