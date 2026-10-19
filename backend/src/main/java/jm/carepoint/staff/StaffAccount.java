package jm.carepoint.staff;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
@Entity @Table(name="staff_accounts")
public class StaffAccount implements UserDetails {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=80) private String username;
 @Column(nullable=false,length=100) private String displayName;
 @Column(nullable=false,length=255) private String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StaffRole role;
 @Column(nullable=false) private boolean enabled=true;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 protected StaffAccount(){}
 public StaffAccount(String username,String displayName,String passwordHash,StaffRole role){this.username=username.trim().toLowerCase();this.displayName=displayName.trim();this.passwordHash=passwordHash;this.role=role;}
 public Long getId(){return id;} @Override public String getUsername(){return username;} public String getDisplayName(){return displayName;}
 public StaffRole getRole(){return role;} public Instant getCreatedAt(){return createdAt;} public void deactivate(){enabled=false;}
 @Override public String getPassword(){return passwordHash;} @Override public Collection<? extends GrantedAuthority> getAuthorities(){return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));}
 @Override public boolean isAccountNonExpired(){return true;} @Override public boolean isAccountNonLocked(){return enabled;} @Override public boolean isCredentialsNonExpired(){return true;} @Override public boolean isEnabled(){return enabled;}
}