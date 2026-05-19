package com.storrego.catalog.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RoleMapper {

    private static final Map<Integer, String> ROLE_MAP = Map.of(
            1, "ROLE_ADMIN",
            2, "ROLE_USER"
    );

    private RoleMapper() {}

    public static List<SimpleGrantedAuthority> toAuthorities(Collection<Integer> roleIds) {
        return roleIds.stream()
                .filter(ROLE_MAP::containsKey)
                .map(id -> new SimpleGrantedAuthority(ROLE_MAP.get(id)))
                .collect(Collectors.toList());
    }
}
