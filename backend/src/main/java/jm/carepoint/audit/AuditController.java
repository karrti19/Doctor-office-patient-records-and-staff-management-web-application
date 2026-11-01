package jm.carepoint.audit;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    private final AuditEventRepository events;

    public AuditController(AuditEventRepository events) {
        this.events = events;
    }

    public record AuditView(Long id, String actorUsername, String action,
                            String targetType, Long targetId, String outcome, Instant createdAt) {}

    @GetMapping
    public List<AuditView> recentEvents() {
        return events.findTop100ByOrderByCreatedAtDesc().stream()
                .map(e -> new AuditView(e.getId(), e.getActorUsername(), e.getAction(),
                        e.getTargetType(), e.getTargetId(), e.getOutcome(), e.getCreatedAt()))
                .toList();
    }
}
