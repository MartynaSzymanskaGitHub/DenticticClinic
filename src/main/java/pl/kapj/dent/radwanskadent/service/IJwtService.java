package pl.kapj.dent.radwanskadent.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
    String generateToken(UserDetails userDetails);
    Claims extractAllClaims(String token);
    Cookie createJwtCookie(String token);
}
