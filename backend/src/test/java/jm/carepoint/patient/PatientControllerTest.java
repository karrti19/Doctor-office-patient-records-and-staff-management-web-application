package jm.carepoint.patient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PatientControllerTest {
    private final PatientRepository patients = mock(PatientRepository.class);
    private final PatientController controller = new PatientController(patients);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test-user", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void receptionistCreateResponseDoesNotContainClinicalFields() {
        authenticate("RECEPTIONIST");
        when(patients.save(any(PatientRecord.class))).thenAnswer(call -> call.getArgument(0));

        Object response = controller.create(new PatientController.DemographicsRequest(
                "Jordan Sample", LocalDate.of(1990, 1, 1), "Fictional address", "555-0100"));

        assertInstanceOf(PatientController.ReceptionView.class, response);
    }

    @Test
    void doctorDetailIncludesClinicalFields() {
        authenticate("DOCTOR");
        PatientRecord patient = new PatientRecord(
                "Jordan Sample", LocalDate.of(1990, 1, 1), "Fictional address", "555-0100");
        patient.updateClinical("Fictional clinical note", "Fictional prescription");
        when(patients.findById(1L)).thenReturn(java.util.Optional.of(patient));

        Object response = controller.detail(1L);

        assertInstanceOf(PatientController.PatientView.class, response);
        PatientController.PatientView view = (PatientController.PatientView) response;
        assertEquals("Fictional clinical note", view.clinicalNotes());
        assertEquals("Fictional prescription", view.prescriptionNotes());
    }
}
