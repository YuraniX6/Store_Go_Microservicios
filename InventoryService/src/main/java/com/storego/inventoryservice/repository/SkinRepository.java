package com.storego.inventoryservice.repository;

import com.storego.inventoryservice.entity.Skin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkinRepository extends JpaRepository<Skin, UUID> {
    List<Skin> findAllByOwnerId(UUID ownerId);
}
