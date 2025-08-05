package org.burgas.backendserver.repository.image;

import org.burgas.backendserver.entity.Image;
import org.burgas.backendserver.repository.MasterRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@MasterRepository
public interface ImageMasterRepository extends JpaRepository<Image, UUID> {
}
