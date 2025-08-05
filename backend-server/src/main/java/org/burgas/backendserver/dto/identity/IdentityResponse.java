package org.burgas.backendserver.dto.identity;

import lombok.*;
import org.burgas.backendserver.dto.Response;
import org.burgas.backendserver.entity.Authority;
import org.burgas.backendserver.entity.Image;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class IdentityResponse extends Response {

    private UUID id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Authority authority;
    private Boolean enabled;
    private Image image;
}
