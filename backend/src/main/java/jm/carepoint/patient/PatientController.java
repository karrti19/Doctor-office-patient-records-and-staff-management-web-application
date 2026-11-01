package jm.carepoint.patient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientRepository patients;

    public PatientController(PatientRepository patients) {
        this.patients = patients;
    }

    public record DemographicsRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotNull @PastOrPresent LocalDate dateOfBirth,
            @Size(max = 180) String address,
            @Size(max = 30) String phone) {}

    public record ClinicalRequest(
            @Size(max = 2000) String clinicalNotes,
            @Size(max = 2000) String prescriptionNotes) {}

    public record PatientView(Long id, String fullName, LocalDate dateOfBirth,
                              String address, String phone, String clinicalNotes,
                              String prescriptionNotes, Instant createdAt, Instant updatedAt) {}

    public record ReceptionView(Long id, String fullName, LocalDate dateOfBirth,
                                String address, String phone, Instant updatedAt) {}

    private PatientRecord get(Long id) {
        return patients.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found."));
    }

    private boolean canReadClinical() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public Object list(@RequestParam(defaultValue = "") @Size(max = 100) String q) {
        List<PatientRecord> found = patients.search(q.trim());
        return canReadClinical()
                ? found.stream().map(this::clinicalView).toList()
                : found.stream().map(this::receptionView).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public Object detail(@PathVariable Long id) {
        PatientRecord patient = get(id);
        return canReadClinical() ? clinicalView(patient) : receptionView(patient);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public Object create(@Valid @RequestBody DemographicsRequest request) {
        PatientRecord saved = patients.save(new PatientRecord(
                request.fullName(), request.dateOfBirth(), request.address(), request.phone()));
        return canReadClinical() ? clinicalView(saved) : receptionView(saved);
    }

    @PutMapping("/{id}/demographics")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    public Object updateDemographics(@PathVariable Long id, @Valid @RequestBody DemographicsRequest request) {
        PatientRecord patient = get(id);
        patient.updateDemographics(request.fullName(), request.dateOfBirth(), request.address(), request.phone());
        patients.save(patient);
        return canReadClinical() ? clinicalView(patient) : receptionView(patient);
    }

    @PutMapping("/{id}/clinical")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public PatientView updateClinical(@PathVariable Long id, @Valid @RequestBody ClinicalRequest request) {
        PatientRecord patient = get(id);
        patient.updateClinical(request.clinicalNotes(), request.prescriptionNotes());
        return clinicalView(patients.save(patient));
    }

    private PatientView clinicalView(PatientRecord p) {
        return new PatientView(p.getId(), p.getFullName(), p.getDateOfBirth(), p.getAddress(),
                p.getPhone(), p.getClinicalNotes(), p.getPrescriptionNotes(), p.getCreatedAt(), p.getUpdatedAt());
    }

    private ReceptionView receptionView(PatientRecord p) {
        return new ReceptionView(p.getId(), p.getFullName(), p.getDateOfBirth(),
                p.getAddress(), p.getPhone(), p.getUpdatedAt());
    }
}
