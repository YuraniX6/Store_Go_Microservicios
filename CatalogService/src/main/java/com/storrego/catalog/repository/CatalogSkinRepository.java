package com.storrego.catalog.repository;

import com.storrego.catalog.entity.CatalogSkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogSkinRepository
        extends JpaRepository<CatalogSkin, String>, JpaSpecificationExecutor<CatalogSkin> {
}
