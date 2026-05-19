package com.storrego.catalog.repository;

import com.storrego.catalog.entity.CatalogSkin;
import org.springframework.data.jpa.domain.Specification;

public final class CatalogSkinSpecifications {

    private CatalogSkinSpecifications() {}

    public static Specification<CatalogSkin> weaponNameEquals(String weaponName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("weaponName")), weaponName.toLowerCase());
    }

    public static Specification<CatalogSkin> categoryNameEquals(String categoryName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("categoryName")), categoryName.toLowerCase());
    }

    public static Specification<CatalogSkin> rarityNameEquals(String rarityName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("rarityName")), rarityName.toLowerCase());
    }

    public static Specification<CatalogSkin> stattrakEquals(Boolean stattrak) {
        return (root, query, cb) ->
                cb.equal(root.get("stattrak"), stattrak);
    }

    public static Specification<CatalogSkin> souvenirEquals(Boolean souvenir) {
        return (root, query, cb) ->
                cb.equal(root.get("souvenir"), souvenir);
    }

    public static Specification<CatalogSkin> nameLike(String q) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%");
    }
}
