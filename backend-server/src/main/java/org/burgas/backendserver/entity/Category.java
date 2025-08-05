package org.burgas.backendserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "category-with-products",
        attributeNodes = @NamedAttributeNode(value = "products", subgraph = "productSubgraph"),
        subgraphs = @NamedSubgraph(name = "productSubgraph", type = Product.class, attributeNodes = @NamedAttributeNode("images"))
)
public class Category extends AbstractEntity implements Serializable {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "category", cascade = ALL, fetch = EAGER)
    private List<Product> products;
}
