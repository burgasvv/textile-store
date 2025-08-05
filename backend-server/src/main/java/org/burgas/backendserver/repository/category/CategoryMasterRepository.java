package org.burgas.backendserver.repository.category;

import org.burgas.backendserver.entity.Category;
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
public interface CategoryMasterRepository extends JpaRepository<Category, UUID> {

    @Override
    @EntityGraph(attributePaths = {"products"})
    @NotNull List<Category> findAll();

    @Override
    @EntityGraph(value = "category-with-products", type = EntityGraph.EntityGraphType.FETCH)
    @NotNull Optional<Category> findById(@NotNull UUID uuid);
}
