package jm.carepoint.patient;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
@Entity @Table(name="patient_records")
public class PatientRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String fullName;
 @Column(nullable=false) private LocalDate dateOfBirth;
 @Column(length=180) private String address;
 @Column(length=30) private String phone;
 @Column(length=2000) private String clinicalNotes;
 @Column(length=2000) private String prescriptionNotes;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 @Column(nullable=false) private Instant updatedAt=Instant.now();
 protected PatientRecord(){}
 public PatientRecord(String name,LocalDate dob,String address,String phone){this.fullName=name.trim();this.dateOfBirth=dob;this.address=address;this.phone=phone;}
 @PreUpdate void updateTimestamp(){updatedAt=Instant.now();}
 public Long getId(){return id;} public String getFullName(){return fullName;} public LocalDate getDateOfBirth(){return dateOfBirth;}
 public String getAddress(){return address;} public String getPhone(){return phone;} public String getClinicalNotes(){return clinicalNotes;}
 public String getPrescriptionNotes(){return prescriptionNotes;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
 public void updateDemographics(String name,LocalDate dob,String address,String phone){this.fullName=name.trim();this.dateOfBirth=dob;this.address=address;this.phone=phone;}
 public void updateClinical(String notes,String prescription){this.clinicalNotes=notes;this.prescriptionNotes=prescription;}
}