package com.springforge.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, String> {
    List<OrganizationMember> findByOrganizationId(String organizationId);
    long countByOrganizationId(String organizationId);
    void deleteByOrganizationIdAndUserId(String organizationId, String userId);
}
