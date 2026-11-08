package pl.kapj.dent.radwanskadent.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.kapj.dent.radwanskadent.config.JwtAuthenticationFilter;
import pl.kapj.dent.radwanskadent.service.IJwtService;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Service
public class JwtService implements IJwtService {

    private final SecretKey signingKey;
    private final long jwtExpirationMs;
    private final boolean cookieSecure;

    public JwtService(
            @Value("${jwt.secret:bXktdmVyeS1zZWNyZXQta2V5LWZvci1yYWR3YW5za2EtZGVudC0yMDI2LWthcGotcHJvamVjdA==}") String secretKey,
            @Value("${jwt.expiration:86400000}") long jwtExpirationMs,
            @Value("${jwt.cookie-secure:false}") boolean cookieSecure) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.jwtExpirationMs = jwtExpirationMs;
        this.cookieSecure = cookieSecure;
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public Cookie createJwtCookie(String token) {
        Cookie jwtCookie = new Cookie(JwtAuthenticationFilter.JWT_COOKIE_NAME, token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(cookieSecure);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) Duration.ofMillis(jwtExpirationMs).toSeconds());
        jwtCookie.setAttribute("SameSite", "Lax");
        return jwtCookie;
    }
}
