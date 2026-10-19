package jm.carepoint.bootstrap;
import jm.carepoint.staff.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.env.Environment;
import java.util.Arrays;
@Configuration public class InitialAdminBootstrap {
 @Bean ApplicationRunner createInitialAdmin(StaffRepository staff,PasswordEncoder encoder,Environment env){return args->{
  String username=env.getProperty("INITIAL_ADMIN_USERNAME"),password=env.getProperty("INITIAL_ADMIN_PASSWORD");
  boolean production=Arrays.asList(env.getActiveProfiles()).contains("prod");
  if(production&&(username==null||username.isBlank()||password==null||password.length()<20))throw new IllegalStateException("Production requires INITIAL_ADMIN_USERNAME and a unique INITIAL_ADMIN_PASSWORD of at least 20 characters.");
  if(username!=null&&!username.isBlank()&&password!=null&&!password.isBlank()&&!staff.existsByUsernameIgnoreCase(username))
   staff.save(new StaffAccount(username,"Initial Administrator",encoder.encode(password),StaffRole.ADMIN));
 };}
}