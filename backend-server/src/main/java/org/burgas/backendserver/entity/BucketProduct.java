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
@Table(name = "bucket_product")
@EqualsAndHashCode(callSuper = true)
@IdClass(value = BucketProductPK.class)
public class BucketProduct extends AbstractEntity implements Serializable {

    @Id
    @Column(name = "bucket_id")
    private UUID bucketId;

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "cost")
    private Double cost;
}
