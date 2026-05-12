package com.springforge.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    public Page<AdminUser> listUsers(Pageable pageable) {
        return adminUserRepository.findAll(pageable);
    }

    public AdminUser createUser(AdminUser user) {
        return adminUserRepository.save(user);
    }

    public AdminUser updateUser(String id, AdminUser updated) {
        AdminUser existing = adminUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
        existing.setUsername(updated.getUsername());
        existing.setEmail(updated.getEmail());
        return adminUserRepository.save(existing);
    }

    public AdminUser changeRole(String id, UserRole newRole) {
        AdminUser user = adminUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setRole(newRole);
        return adminUserRepository.save(user);
    }

    public void deactivateUser(String id) {
        AdminUser user = adminUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(false);
        adminUserRepository.save(user);
    }

    public long getActiveUserCount() {
        return adminUserRepository.countByActiveTrue();
    }
}
