package jm.carepoint.staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StaffRepository extends JpaRepository<StaffAccount,Long>{
 Optional<StaffAccount> findByUsernameIgnoreCaseAndEnabledTrue(String username);
 boolean existsByUsernameIgnoreCase(String username);
}