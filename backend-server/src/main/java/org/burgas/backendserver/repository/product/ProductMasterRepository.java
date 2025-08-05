package org.burgas.backendserver.repository.product;

import org.burgas.backendserver.entity.Product;
import org.burgas.backendserver.repository.MasterRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@MasterRepository
public interface ProductMasterRepository extends JpaRepository<Product, UUID> {

    @Override
    @EntityGraph(value = "products-with-images")
    @NotNull List<Product> findAll();

    @Override
    @EntityGraph(value = "products-with-images")
    @NotNull Optional<Product> findById(@NotNull UUID uuid);
}
