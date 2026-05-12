package com.springforge.admin;

import com.springforge.marketplace.Blueprint;
import com.springforge.marketplace.BlueprintService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final AdminUserService adminUserService;
    private final AuditService auditService;
    private final BlueprintService blueprintService;

    public AdminController(DashboardService dashboardService,
                          AdminUserService adminUserService,
                          AuditService auditService,
                          BlueprintService blueprintService) {
        this.dashboardService = dashboardService;
        this.adminUserService = adminUserService;
        this.auditService = auditService;
        this.blueprintService = blueprintService;
    }

    @GetMapping("/dashboard")
    public DashboardStats getDashboard() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/users")
    public Page<AdminUser> listUsers(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return adminUserService.listUsers(PageRequest.of(page, size));
    }

    @PostMapping("/users")
    public AdminUser createUser(@RequestBody AdminUser user) {
        return adminUserService.createUser(user);
    }

    @PutMapping("/users/{id}")
    public AdminUser updateUser(@PathVariable String id, @RequestBody AdminUser user) {
        return adminUserService.updateUser(id, user);
    }

    @PutMapping("/users/{id}/role")
    public AdminUser changeRole(@PathVariable String id, @RequestBody RoleChangeRequest request) {
        return adminUserService.changeRole(id, request.role());
    }

    @DeleteMapping("/users/{id}")
    public void deactivateUser(@PathVariable String id) {
        adminUserService.deactivateUser(id);
    }

    @GetMapping("/audit")
    public Page<AuditLog> getAuditLogs(@RequestParam(required = false) String action,
                                        @RequestParam(required = false) String userId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (userId != null) return auditService.getLogsByUser(userId, pageable);
        if (action != null) return auditService.getLogsByAction(action, pageable);
        return auditService.getRecentLogs(pageable);
    }

    @GetMapping("/blueprints/pending")
    public List<Blueprint> getPendingBlueprints() {
        return blueprintService.getPendingApproval();
    }

    @PutMapping("/blueprints/{id}/approve")
    public Blueprint approveBlueprint(@PathVariable String id) {
        return blueprintService.approve(id);
    }

    @PutMapping("/blueprints/{id}/reject")
    public void rejectBlueprint(@PathVariable String id) {
        blueprintService.delete(id);
    }

    public record RoleChangeRequest(UserRole role) {}
}
