package com.BusBooking.User.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.BusBooking.User.exception.InvalidRequestBodyException;
import com.BusBooking.User.exception.InvalidTokenException;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("$jwt.Secret")
    private String jwtSecret;

    private int jwtExpirationInMs=3600000;     //1 hour expiration time
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    public Mono<String> generateToken(String emailId,String role) {
        return Mono.fromCallable(() -> Jwts.builder()
                .setSubject(emailId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes())
                .compact());
    }

    public Mono<String> extractUsername(String token) {
        return Mono.fromCallable(() ->Jwts.parser()
                .setSigningKey(jwtSecret.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }
    public Mono<String> extractRole(String token) {
        return Mono.fromCallable(() ->Jwts.parser()
                .setSigningKey(jwtSecret.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class)); // Extracting the 'role' claim
    }
    public Mono<Boolean> validateToken(String token, String username) 
    {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String tokenUsername = claims.getSubject(); 
            Date expiration = claims.getExpiration(); 
            if(username.equals(tokenUsername) && !expiration.before(new Date()))
            {
                return Mono.just(true); 
            }
            return Mono.error(new InvalidTokenException("JWT validation error"));

        } catch ( ExpiredJwtException ex) {
            return Mono.error(new InvalidTokenException("Token Expired"));
        }
        catch (Exception e) {
            return Mono.error(new InvalidTokenException("JWT validation error"));
        }
    
    }
    public Mono<String> getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return Mono.just(bearerToken.substring(TOKEN_PREFIX.length()));
        }
        return Mono.error(new InvalidRequestBodyException("Token Missing"));
    }
}
