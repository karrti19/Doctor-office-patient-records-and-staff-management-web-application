package jm.carepoint.staff;

import jm.carepoint.audit.AuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class StaffAdminController {
    private final StaffRepository staff;
    private final PasswordEncoder encoder;
    private final SessionRegistry sessionRegistry;
    private final AuditService audit;

    public StaffAdminController(StaffRepository staff, PasswordEncoder encoder,
                                SessionRegistry sessionRegistry, AuditService audit) {
        this.staff = staff;
        this.encoder = encoder;
        this.sessionRegistry = sessionRegistry;
        this.audit = audit;
    }

    public record CreateStaffRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,80}") String username,
            @NotBlank @Size(min = 2, max = 100) String displayName,
            @NotBlank @Size(min = 14, max = 128) String temporaryPassword,
            @NotNull StaffRole role) {}

    public record UpdateStaffRequest(
            @NotBlank @Size(min = 2, max = 100) String displayName,
            @NotNull Boolean enabled) {}

    public record ResetPasswordRequest(
            @NotBlank @Size(min = 14, max = 128) String temporaryPassword) {}

    public record StaffView(Long id, String username, String displayName, StaffRole role,
                            boolean enabled, Instant createdAt) {
        static StaffView from(StaffAccount account) {
            return new StaffView(account.getId(), account.getUsername(), account.getDisplayName(),
                    account.getRole(), account.isEnabled(), account.getCreatedAt());
        }
    }

    @GetMapping
    public List<StaffView> list() {
        audit.record("STAFF_DIRECTORY_VIEW", "STAFF", null);
        return staff.findAll().stream().map(StaffView::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffView create(@Valid @RequestBody CreateStaffRequest request) {
        if (request.role() == StaffRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Administrator accounts require a separately reviewed process.");
        }
        String username = request.username().trim().toLowerCase();
        if (staff.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use.");
        }
        StaffAccount created = new StaffAccount(username, request.displayName(),
                encoder.encode(request.temporaryPassword()), request.role());
        StaffAccount saved = staff.save(created);
        audit.record("STAFF_ACCOUNT_CREATED", "STAFF", saved.getId());
        return StaffView.from(saved);
    }

    @PutMapping("/{id}")
    public StaffView update(@PathVariable Long id, @Valid @RequestBody UpdateStaffRequest request) {
        StaffAccount account = getStaff(id);
        if (account.getRole() == StaffRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Administrator account changes require a separately reviewed process.");
        }
        account.updateProfile(request.displayName(), account.getRole());
        account.setEnabled(request.enabled());
        StaffAccount saved = staff.save(account);
        audit.record(request.enabled() ? "STAFF_PROFILE_UPDATED" : "STAFF_ACCOUNT_DISABLED", "STAFF", id);
        if (!request.enabled()) {
            expireSessions(account.getUsername());
        }
        return StaffView.from(saved);
    }

    @PostMapping("/{id}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        StaffAccount account = getStaff(id);
        if (account.getRole() == StaffRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Administrator password resets require a separately reviewed process.");
        }
        account.updatePassword(encoder.encode(request.temporaryPassword()));
        staff.save(account);
        audit.record("STAFF_PASSWORD_RESET", "STAFF", id);
        expireSessions(account.getUsername());
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        StaffAccount account = getStaff(id);
        if (account.getRole() == StaffRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Administrator deactivation requires a separate recovery procedure.");
        }
        account.deactivate();
        staff.save(account);
        audit.record("STAFF_ACCOUNT_DEACTIVATED", "STAFF", id);
        expireSessions(account.getUsername());
    }

    private void expireSessions(String username) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof UserDetails user
                        && user.getUsername().equalsIgnoreCase(username))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .forEach(SessionInformation::expireNow);
    }

    private StaffAccount getStaff(Long id) {
        return staff.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff account not found."));
    }
}
