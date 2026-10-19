package jm.carepoint.staff;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
@RestController @RequestMapping("/api/admin/staff") @PreAuthorize("hasRole('ADMIN')")
public class StaffAdminController {
 private final StaffRepository staff; private final PasswordEncoder encoder;
 public StaffAdminController(StaffRepository staff,PasswordEncoder encoder){this.staff=staff;this.encoder=encoder;}
 public record CreateStaffRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9._-]{3,80}") String username,@NotBlank @Size(min=2,max=100) String displayName,@NotBlank @Size(min=14,max=128) String temporaryPassword,StaffRole role){}
 public record StaffView(Long id,String username,String displayName,StaffRole role,boolean enabled,Instant createdAt){static StaffView from(StaffAccount s){return new StaffView(s.getId(),s.getUsername(),s.getDisplayName(),s.getRole(),s.isEnabled(),s.getCreatedAt());}}
 @GetMapping public List<StaffView> list(){return staff.findAll().stream().map(StaffView::from).toList();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public StaffView create(@Valid @RequestBody CreateStaffRequest r){
  if(r.role()==null||r.role()==StaffRole.ADMIN)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Administrator accounts require a separately reviewed process.");
  String username=r.username().trim().toLowerCase();if(staff.existsByUsernameIgnoreCase(username))throw new ResponseStatusException(HttpStatus.CONFLICT,"Username is already in use.");
  return StaffView.from(staff.save(new StaffAccount(username,r.displayName(),encoder.encode(r.temporaryPassword()),r.role())));
 }
 @PostMapping("/{id}/deactivate") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(@PathVariable Long id){
  StaffAccount a=staff.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(a.getRole()==StaffRole.ADMIN)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Administrator deactivation requires a separate recovery procedure.");
  a.deactivate();staff.save(a);
 }
}