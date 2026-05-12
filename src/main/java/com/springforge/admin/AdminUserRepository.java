package com.springforge.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, String> {

    Page<AdminUser> findByActiveTrue(Pageable pageable);

    long countByActiveTrue();

    java.util.Optional<AdminUser> findByEmail(String email);
}
