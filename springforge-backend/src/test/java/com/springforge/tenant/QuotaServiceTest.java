package com.springforge.tenant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotaServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @InjectMocks
    private QuotaService quotaService;

    private Organization freeOrg;
    private Organization enterpriseOrg;

    @BeforeEach
    void setUp() {
        freeOrg = new Organization();
        freeOrg.setId("org-free");
        freeOrg.setPlan(SubscriptionPlan.FREE);
        freeOrg.setMaxProjects(5);
        freeOrg.setMaxUsers(2);
        freeOrg.setCurrentProjectCount(3);
        freeOrg.setCurrentUserCount(1);

        enterpriseOrg = new Organization();
        enterpriseOrg.setId("org-ent");
        enterpriseOrg.setPlan(SubscriptionPlan.ENTERPRISE);
        enterpriseOrg.setMaxProjects(-1);
        enterpriseOrg.setMaxUsers(-1);
    }

    @Test
    void shouldAllowProjectWhenUnderQuota() {
        when(organizationRepository.findById("org-free")).thenReturn(Optional.of(freeOrg));

        assertThat(quotaService.checkProjectQuota("org-free")).isTrue();
    }

    @Test
    void shouldDenyProjectWhenAtQuota() {
        freeOrg.setCurrentProjectCount(5);
        when(organizationRepository.findById("org-free")).thenReturn(Optional.of(freeOrg));

        assertThat(quotaService.checkProjectQuota("org-free")).isFalse();
    }

    @Test
    void shouldAlwaysAllowForEnterprise() {
        when(organizationRepository.findById("org-ent")).thenReturn(Optional.of(enterpriseOrg));

        assertThat(quotaService.checkProjectQuota("org-ent")).isTrue();
        assertThat(quotaService.checkUserQuota("org-ent")).isTrue();
        assertThat(quotaService.checkGenerationQuota("org-ent")).isTrue();
    }

    @Test
    void shouldIncrementProjectCount() {
        when(organizationRepository.findById("org-free")).thenReturn(Optional.of(freeOrg));
        when(organizationRepository.save(any())).thenReturn(freeOrg);

        quotaService.incrementProjectCount("org-free");

        assertThat(freeOrg.getCurrentProjectCount()).isEqualTo(4);
        verify(organizationRepository).save(freeOrg);
    }

    @Test
    void shouldReturnUsageReport() {
        when(organizationRepository.findById("org-free")).thenReturn(Optional.of(freeOrg));
        when(memberRepository.countByOrganizationId("org-free")).thenReturn(1L);

        UsageReport report = quotaService.getUsage("org-free");

        assertThat(report.organizationId()).isEqualTo("org-free");
        assertThat(report.plan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(report.projectsUsed()).isEqualTo(3);
        assertThat(report.projectsLimit()).isEqualTo(5);
        assertThat(report.usersUsed()).isEqualTo(1);
        assertThat(report.usersLimit()).isEqualTo(2);
    }
}
