package org.burgas.backendserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "bill_product")
@IdClass(value = BillProductPK.class)
public class BillProduct extends AbstractEntity implements Serializable {

    @Id
    @Column(name = "bill_id")
    private UUID billId;

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "cost")
    private Double cost;
}
