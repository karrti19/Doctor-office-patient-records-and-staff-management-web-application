package jm.carepoint.staff;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StaffAdminControllerTest {
    private final StaffRepository staff = mock(StaffRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final StaffAdminController controller = new StaffAdminController(staff, encoder);

    @Test
    void administratorCanUpdateOrdinaryStaffProfileAndStatus() {
        StaffAccount account = new StaffAccount("doctor.one", "Doctor One", "hash", StaffRole.DOCTOR);
        when(staff.findById(5L)).thenReturn(Optional.of(account));
        when(staff.save(any(StaffAccount.class))).thenAnswer(call -> call.getArgument(0));

        StaffAdminController.StaffView result = controller.update(5L,
                new StaffAdminController.UpdateStaffRequest("Updated Doctor", StaffRole.RECEPTIONIST, false));

        assertEquals("Updated Doctor", result.displayName());
        assertEquals(StaffRole.RECEPTIONIST, result.role());
        assertFalse(result.enabled());
    }

    @Test
    void ordinaryStaffCannotBePromotedToAdministrator() {
        StaffAccount account = new StaffAccount("staff.one", "Staff One", "hash", StaffRole.DOCTOR);
        when(staff.findById(5L)).thenReturn(Optional.of(account));

        assertThrows(ResponseStatusException.class, () -> controller.update(5L,
                new StaffAdminController.UpdateStaffRequest("Staff One", StaffRole.ADMIN, true)));
        verify(staff, never()).save(any(StaffAccount.class));
    }

    @Test
    void passwordResetStoresOnlyEncodedPassword() {
        StaffAccount account = new StaffAccount("doctor.one", "Doctor One", "old-hash", StaffRole.DOCTOR);
        when(staff.findById(5L)).thenReturn(Optional.of(account));
        when(encoder.encode("a-long-temporary-password")).thenReturn("encoded-hash");
        when(staff.save(any(StaffAccount.class))).thenAnswer(call -> call.getArgument(0));

        controller.resetPassword(5L,
                new StaffAdminController.ResetPasswordRequest("a-long-temporary-password"));

        assertEquals("encoded-hash", account.getPassword());
    }
}
