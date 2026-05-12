package com.springforge.tenant;

import org.springframework.stereotype.Service;

@Service
public class QuotaService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;

    public QuotaService(OrganizationRepository organizationRepository,
                       OrganizationMemberRepository memberRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
    }

    public boolean checkProjectQuota(String orgId) {
        Organization org = getOrg(orgId);
        if (org.getPlan().isUnlimited()) return true;
        return org.getCurrentProjectCount() < org.getMaxProjects();
    }

    public boolean checkUserQuota(String orgId) {
        Organization org = getOrg(orgId);
        if (org.getPlan().isUnlimited()) return true;
        return org.getCurrentUserCount() < org.getMaxUsers();
    }

    public boolean checkGenerationQuota(String orgId) {
        Organization org = getOrg(orgId);
        if (org.getPlan().isUnlimited()) return true;
        return org.getPlan().getMaxGenerationsPerDay() > 0;
    }

    public void incrementProjectCount(String orgId) {
        Organization org = getOrg(orgId);
        org.setCurrentProjectCount(org.getCurrentProjectCount() + 1);
        organizationRepository.save(org);
    }

    public UsageReport getUsage(String orgId) {
        Organization org = getOrg(orgId);
        long memberCount = memberRepository.countByOrganizationId(orgId);

        return new UsageReport(
            orgId,
            org.getPlan(),
            org.getCurrentProjectCount(),
            org.getMaxProjects(),
            (int) memberCount,
            org.getMaxUsers(),
            0,
            org.getPlan().getMaxGenerationsPerDay(),
            0,
            org.getPlan().isUnlimited() ? -1 : 1024
        );
    }

    private Organization getOrg(String orgId) {
        return organizationRepository.findById(orgId)
            .orElseThrow(() -> new RuntimeException("Organization not found: " + orgId));
    }
}
