package ${packageName}.config.multitenant;

<#if multiTenantStrategy == "DISCRIMINATOR">
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getCurrentTenant();
        }
    }

    public String getTenantId() {
        return tenantId;
    }

    protected void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
<#else>
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TenantAwareEntity {
    // Tenant isolation handled at ${multiTenantStrategy?lower_case} level
}
</#if>
