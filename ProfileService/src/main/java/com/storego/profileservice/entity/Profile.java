package com.storego.profileservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "profiles",
    indexes = {
        @Index(name = "idx_rut", columnList = "rut", unique = true)
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_rut", columnNames = "rut")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "fullname", nullable = false, length = 150)
    private String fullname;

    @Column(name = "rut", nullable = false, length = 20, unique = true)
    private String rut;

    @Column(name = "language", nullable = false, length = 2)
    private String language;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
