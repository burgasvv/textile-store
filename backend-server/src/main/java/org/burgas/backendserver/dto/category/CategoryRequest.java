package org.burgas.backendserver.dto.category;

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
public class CategoryRequest extends Request {

    private UUID id;
    private String name;
}
