package jm.carepoint.security;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class CsrfController {
 @GetMapping("/api/csrf") public String csrf(CsrfToken token){return token.getToken();}
}