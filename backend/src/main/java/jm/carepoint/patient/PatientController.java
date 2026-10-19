package jm.carepoint.patient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/api/patients")
public class PatientController {
 private final PatientRepository patients;
 public PatientController(PatientRepository patients){this.patients=patients;}
 public record DemographicsRequest(@NotBlank @Size(max=100) String fullName,@NotNull LocalDate dateOfBirth,@Size(max=180) String address,@Size(max=30) String phone){}
 public record ClinicalRequest(@Size(max=2000) String clinicalNotes,@Size(max=2000) String prescriptionNotes){}
 public record PatientView(Long id,String fullName,LocalDate dateOfBirth,String address,String phone,String clinicalNotes,String prescriptionNotes,Instant createdAt,Instant updatedAt){}
 public record ReceptionView(Long id,String fullName,LocalDate dateOfBirth,String address,String phone,Instant updatedAt){}
 private PatientRecord get(Long id){return patients.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private boolean canReadClinical(){return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_DOCTOR")||a.getAuthority().equals("ROLE_ADMIN"));}
 @GetMapping public Object list(@RequestParam(defaultValue="") @Size(max=100) String q){List<PatientRecord> found=patients.search(q);return canReadClinical()?found.stream().map(this::clinicalView).toList():found.stream().map(this::receptionView).toList();}
 @GetMapping("/{id}") public Object detail(@PathVariable Long id){PatientRecord p=get(id);return canReadClinical()?clinicalView(p):receptionView(p);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
 public PatientView create(@Valid @RequestBody DemographicsRequest r){return clinicalView(patients.save(new PatientRecord(r.fullName(),r.dateOfBirth(),r.address(),r.phone())));}
 @PutMapping("/{id}/demographics") @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
 public Object updateDemographics(@PathVariable Long id,@Valid @RequestBody DemographicsRequest r){PatientRecord p=get(id);p.updateDemographics(r.fullName(),r.dateOfBirth(),r.address(),r.phone());patients.save(p);return canReadClinical()?clinicalView(p):receptionView(p);}
 @PutMapping("/{id}/clinical") @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
 public PatientView updateClinical(@PathVariable Long id,@Valid @RequestBody ClinicalRequest r){PatientRecord p=get(id);p.updateClinical(r.clinicalNotes(),r.prescriptionNotes());return clinicalView(patients.save(p));}
 private PatientView clinicalView(PatientRecord p){return new PatientView(p.getId(),p.getFullName(),p.getDateOfBirth(),p.getAddress(),p.getPhone(),p.getClinicalNotes(),p.getPrescriptionNotes(),p.getCreatedAt(),p.getUpdatedAt());}
 private ReceptionView receptionView(PatientRecord p){return new ReceptionView(p.getId(),p.getFullName(),p.getDateOfBirth(),p.getAddress(),p.getPhone(),p.getUpdatedAt());}
}