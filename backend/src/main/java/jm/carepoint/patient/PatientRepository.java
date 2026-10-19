package jm.carepoint.patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface PatientRepository extends JpaRepository<PatientRecord,Long>{
 @Query("select p from PatientRecord p where lower(p.fullName) like lower(concat('%',:q,'%')) order by p.fullName")
 List<PatientRecord> search(@Param("q") String query);
}