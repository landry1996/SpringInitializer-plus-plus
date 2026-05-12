package com.springforge.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {
    List<ApiKey> findByOrganizationIdAndActiveTrue(String organizationId);
    Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);
}
