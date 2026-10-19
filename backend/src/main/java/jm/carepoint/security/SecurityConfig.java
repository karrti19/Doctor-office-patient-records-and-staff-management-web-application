package jm.carepoint.security;
import jm.carepoint.staff.StaffRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean DaoAuthenticationProvider authenticationProvider(StaffRepository repo,PasswordEncoder encoder){
  DaoAuthenticationProvider provider=new DaoAuthenticationProvider(user->repo.findByUsernameIgnoreCaseAndEnabledTrue(user).orElseThrow(()->new UsernameNotFoundException("Invalid username or password")));
  provider.setPasswordEncoder(encoder);return provider;
 }
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,DaoAuthenticationProvider provider)throws Exception{
  http.authenticationProvider(provider)
   .csrf(csrf->csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
   .authorizeHttpRequests(auth->auth.requestMatchers("/login","/error","/actuator/health","/css/**","/js/**","/api/csrf").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET,"/api/patients/**").hasAnyRole("ADMIN","DOCTOR","RECEPTIONIST")
    .requestMatchers("/api/patients/**").hasAnyRole("ADMIN","DOCTOR","RECEPTIONIST")
    .anyRequest().authenticated())
   .formLogin(form->form.defaultSuccessUrl("/",true))
   .logout(logout->logout.logoutSuccessUrl("/login?logout").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
   .sessionManagement(session->session.sessionFixation(fix->fix.migrateSession()));
  return http.build();
 }
}