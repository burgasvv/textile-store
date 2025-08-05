package org.burgas.backendserver.dto.identity;

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
public class IdentityRequest extends Request {

    private UUID id;
    private String username;
    private String password;
    private String email;
    private String phone;
}
