package com.springforge.shared.config;

import com.springforge.shared.security.AuthenticatedUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLoggingAspect {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object auditMutations(ProceedingJoinPoint joinPoint) throws Throwable {
        String user = getCurrentUser();
        String method = joinPoint.getSignature().toShortString();

        audit.info("ACTION user={} method={}", user, method);
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            audit.info("SUCCESS user={} method={} duration={}ms", user, method, System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            audit.warn("FAILURE user={} method={} error={}", user, method, e.getMessage());
            throw e;
        }
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser u) {
            return u.email();
        }
        return "anonymous";
    }
}
