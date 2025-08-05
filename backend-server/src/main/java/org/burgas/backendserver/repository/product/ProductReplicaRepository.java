package org.burgas.backendserver.repository.product;

import org.burgas.backendserver.entity.Product;
import org.burgas.backendserver.repository.ReplicaRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ReplicaRepository
public interface ProductReplicaRepository extends JpaRepository<Product, UUID> {

    @Override
    @EntityGraph(value = "products-with-images")
    @NotNull List<Product> findAll();

    @Override
    @EntityGraph(value = "products-with-images")
    @NotNull Optional<Product> findById(@NotNull UUID uuid);

    @Query(
            nativeQuery = true,
            value = """
                    select p.* from replica_postgres_db.public.product p
                                        join public.bucket_product bp on p.id = bp.product_id
                                        where bp.bucket_id = :bucketId
                    """
    )
    List<Product> findProductsInBucket(final UUID bucketId);

    @Query(
            nativeQuery = true,
            value = """
                    select p.* from replica_postgres_db.public.product p
                                        join public.bill_product bp on p.id = bp.product_id
                                        where bp.bill_id = :billId
                    """
    )
    List<Product> findProductInBill(final UUID billId);
}
