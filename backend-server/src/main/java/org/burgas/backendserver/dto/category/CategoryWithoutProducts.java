package org.burgas.backendserver.dto.category;

import lombok.*;
import org.burgas.backendserver.dto.Response;

import java.util.UUID;

@Getter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithoutProducts extends Response {

    private UUID id;
    private String name;
}
