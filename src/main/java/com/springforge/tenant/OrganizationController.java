package com.springforge.tenant;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public Organization create(@RequestBody Organization org) {
        return organizationService.createOrganization(org);
    }

    @GetMapping("/{id}")
    public Organization getById(@PathVariable String id) {
        return organizationService.getUsageReport(id) != null
            ? organizationService.createOrganization(null) // placeholder - would need repo access
            : null;
    }

    @PutMapping("/{id}")
    public Organization update(@PathVariable String id, @RequestBody Organization org) {
        return organizationService.updateOrganization(id, org);
    }

    @GetMapping("/{id}/members")
    public List<OrganizationMember> getMembers(@PathVariable String id) {
        return organizationService.getMembers(id);
    }

    @PostMapping("/{id}/members")
    public OrganizationMember addMember(@PathVariable String id, @RequestBody OrganizationMember member) {
        return organizationService.addMember(id, member);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public void removeMember(@PathVariable String id, @PathVariable String memberId) {
        organizationService.removeMember(id, memberId);
    }

    @PutMapping("/{id}/members/{memberId}/role")
    public OrganizationMember changeMemberRole(@PathVariable String memberId, @RequestBody RoleChangeRequest request) {
        return organizationService.changeMemberRole(memberId, request.role());
    }

    @PostMapping("/{id}/api-keys")
    public ApiKeyResponse generateApiKey(@PathVariable String id, @RequestBody ApiKeyRequest request) {
        String rawKey = organizationService.generateApiKey(id, request.name(), request.scopes());
        return new ApiKeyResponse(rawKey);
    }

    @GetMapping("/{id}/api-keys")
    public List<ApiKey> listApiKeys(@PathVariable String id) {
        return organizationService.listApiKeys(id);
    }

    @DeleteMapping("/{id}/api-keys/{keyId}")
    public void revokeApiKey(@PathVariable String keyId) {
        organizationService.revokeApiKey(keyId);
    }

    @GetMapping("/{id}/usage")
    public UsageReport getUsage(@PathVariable String id) {
        return organizationService.getUsageReport(id);
    }

    @PostMapping("/{id}/upgrade")
    public Organization upgrade(@PathVariable String id, @RequestBody UpgradeRequest request) {
        return organizationService.upgradePlan(id, request.plan());
    }

    public record RoleChangeRequest(MemberRole role) {}
    public record ApiKeyRequest(String name, List<String> scopes) {}
    public record ApiKeyResponse(String key) {}
    public record UpgradeRequest(SubscriptionPlan plan) {}
}
