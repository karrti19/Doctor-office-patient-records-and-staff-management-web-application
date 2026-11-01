package jm.carepoint.staff;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

    public StaffAdminController(StaffRepository staff, PasswordEncoder encoder) {
        this.staff = staff;
        this.encoder = encoder;
    }

    public record CreateStaffRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,80}") String username,
            @NotBlank @Size(min = 2, max = 100) String displayName,
            @NotBlank @Size(min = 14, max = 128) String temporaryPassword,
            @NotNull StaffRole role) {}

    public record UpdateStaffRequest(
            @NotBlank @Size(min = 2, max = 100) String displayName,
            @NotNull StaffRole role,
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
        return StaffView.from(staff.save(created));
    }

    @PutMapping("/{id}")
    public StaffView update(@PathVariable Long id, @Valid @RequestBody UpdateStaffRequest request) {
        StaffAccount account = getStaff(id);
        if (account.getRole() == StaffRole.ADMIN || request.role() == StaffRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Administrator role changes require a separately reviewed process.");
        }
        account.updateProfile(request.displayName(), request.role());
        account.setEnabled(request.enabled());
        return StaffView.from(staff.save(account));
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
    }

    private StaffAccount getStaff(Long id) {
        return staff.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff account not found."));
    }
}
