package jm.carepoint.security;

import jm.carepoint.staff.StaffAccount;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {
    public record SessionView(String username, String displayName, String role) {}

    @GetMapping("/api/session")
    public SessionView current(@AuthenticationPrincipal StaffAccount account) {
        return new SessionView(account.getUsername(), account.getDisplayName(), account.getRole().name());
    }
}
