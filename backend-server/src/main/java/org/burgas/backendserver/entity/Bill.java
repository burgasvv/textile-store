package org.burgas.backendserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Bill extends AbstractEntity implements Serializable {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "cost")
    private Double cost;

    @ManyToOne(cascade = {PERSIST, MERGE, REFRESH, DETACH}, fetch = EAGER)
    @JoinColumn(name = "identity_id", referencedColumnName = "id")
    private Identity identity;
}
