package com.springforge.tenant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private QuotaService quotaService;

    @InjectMocks
    private OrganizationService service;

    private Organization org;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId("org-1");
        org.setName("Test Org");
        org.setSlug("test-org");
        org.setPlan(SubscriptionPlan.FREE);
        org.setCurrentUserCount(1);
    }

    @Test
    void shouldCreateOrganizationWithFreePlan() {
        Organization newOrg = new Organization();
        newOrg.setName("New Org");
        when(organizationRepository.save(any())).thenReturn(newOrg);

        Organization result = service.createOrganization(newOrg);

        assertThat(result).isNotNull();
        assertThat(newOrg.getPlan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(newOrg.getTrialEndsAt()).isNotNull();
    }

    @Test
    void shouldAddMemberWhenQuotaAllows() {
        when(quotaService.checkUserQuota("org-1")).thenReturn(true);
        when(organizationRepository.findById("org-1")).thenReturn(Optional.of(org));
        when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(organizationRepository.save(any())).thenReturn(org);

        OrganizationMember member = new OrganizationMember();
        member.setEmail("new@test.com");
        member.setRole(MemberRole.MEMBER);

        OrganizationMember result = service.addMember("org-1", member);

        assertThat(result.getOrganizationId()).isEqualTo("org-1");
        assertThat(org.getCurrentUserCount()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenQuotaExceeded() {
        when(quotaService.checkUserQuota("org-1")).thenReturn(false);

        OrganizationMember member = new OrganizationMember();

        assertThatThrownBy(() -> service.addMember("org-1", member))
            .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void shouldGenerateApiKey() {
        when(apiKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String rawKey = service.generateApiKey("org-1", "My Key", List.of("generate", "validate"));

        assertThat(rawKey).startsWith("sf_");
        assertThat(rawKey).hasSizeGreaterThan(10);
        verify(apiKeyRepository).save(any());
    }

    @Test
    void shouldRevokeApiKey() {
        ApiKey key = new ApiKey();
        key.setId("key-1");
        key.setActive(true);
        when(apiKeyRepository.findById("key-1")).thenReturn(Optional.of(key));
        when(apiKeyRepository.save(any())).thenReturn(key);

        service.revokeApiKey("key-1");

        assertThat(key.isActive()).isFalse();
    }

    @Test
    void shouldUpgradePlan() {
        when(organizationRepository.findById("org-1")).thenReturn(Optional.of(org));
        when(organizationRepository.save(any())).thenReturn(org);

        Organization result = service.upgradePlan("org-1", SubscriptionPlan.PRO);

        assertThat(org.getPlan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(org.getMaxProjects()).isEqualTo(50);
        assertThat(org.getMaxUsers()).isEqualTo(10);
    }
}
