package com.springforge.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final QuotaService quotaService;

    public OrganizationService(OrganizationRepository organizationRepository,
                              OrganizationMemberRepository memberRepository,
                              ApiKeyRepository apiKeyRepository,
                              QuotaService quotaService) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.quotaService = quotaService;
    }

    public Organization createOrganization(Organization org) {
        if (org.getPlan() == null) org.setPlan(SubscriptionPlan.FREE);
        org.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        return organizationRepository.save(org);
    }

    public Organization updateOrganization(String id, Organization updated) {
        Organization existing = organizationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
        existing.setName(updated.getName());
        existing.setOwnerEmail(updated.getOwnerEmail());
        return organizationRepository.save(existing);
    }

    public List<OrganizationMember> getMembers(String orgId) {
        return memberRepository.findByOrganizationId(orgId);
    }

    public OrganizationMember addMember(String orgId, OrganizationMember member) {
        if (!quotaService.checkUserQuota(orgId)) {
            throw new QuotaExceededException("User quota exceeded for this organization");
        }
        member.setOrganizationId(orgId);
        OrganizationMember saved = memberRepository.save(member);

        Organization org = organizationRepository.findById(orgId).orElseThrow();
        org.setCurrentUserCount(org.getCurrentUserCount() + 1);
        organizationRepository.save(org);

        return saved;
    }

    public void removeMember(String orgId, String memberId) {
        memberRepository.deleteById(memberId);
        Organization org = organizationRepository.findById(orgId).orElseThrow();
        org.setCurrentUserCount(Math.max(0, org.getCurrentUserCount() - 1));
        organizationRepository.save(org);
    }

    public OrganizationMember changeMemberRole(String memberId, MemberRole newRole) {
        OrganizationMember member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setRole(newRole);
        return memberRepository.save(member);
    }

    public String generateApiKey(String orgId, String name, List<String> scopes) {
        String rawKey = "sf_" + UUID.randomUUID().toString().replace("-", "");
        String keyHash = hashKey(rawKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setOrganizationId(orgId);
        apiKey.setName(name);
        apiKey.setKeyHash(keyHash);
        apiKey.setKeyPrefix(rawKey.substring(0, 11));
        apiKey.setScopes(scopes);
        apiKey.setExpiresAt(LocalDateTime.now().plusYears(1));
        apiKeyRepository.save(apiKey);

        return rawKey;
    }

    public void revokeApiKey(String keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
            .orElseThrow(() -> new RuntimeException("API key not found"));
        key.setActive(false);
        apiKeyRepository.save(key);
    }

    public List<ApiKey> listApiKeys(String orgId) {
        return apiKeyRepository.findByOrganizationIdAndActiveTrue(orgId);
    }

    public Organization upgradePlan(String orgId, SubscriptionPlan newPlan) {
        Organization org = organizationRepository.findById(orgId)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
        org.setPlan(newPlan);
        org.setMaxProjects(newPlan.getMaxProjects());
        org.setMaxUsers(newPlan.getMaxUsers());
        return organizationRepository.save(org);
    }

    public UsageReport getUsageReport(String orgId) {
        return quotaService.getUsage(orgId);
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash key", e);
        }
    }
}
