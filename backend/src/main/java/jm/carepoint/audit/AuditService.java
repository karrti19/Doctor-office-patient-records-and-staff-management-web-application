package jm.carepoint.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditEventRepository events;

    public AuditService(AuditEventRepository events) {
        this.events = events;
    }

    @Transactional
    public void record(String action, String targetType, Long targetId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null || !authentication.isAuthenticated()
                ? "anonymous" : authentication.getName();
        events.save(new AuditEvent(actor, action, targetType, targetId, "SUCCESS"));
    }
}
