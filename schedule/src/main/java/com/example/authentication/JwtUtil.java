package com.example.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.exception.InvalidTokenException;

import java.util.Date;


@Component
public class JwtUtil {

    @Value("$jwt.Secret")
    private String jwtSecret;     
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

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

            return Mono.just(username.equals(tokenUsername) && !expiration.before(new Date())); 
        } 
        catch (ExpiredJwtException ex) {
            return Mono.error(new InvalidTokenException("Token Expired"));
        }
        catch(MalformedJwtException ex){
            return Mono.error(new InvalidTokenException("Token Malformed"));
        }
        catch (SignatureException | UnsupportedJwtException | IllegalArgumentException ex) {
            return Mono.error(new InvalidTokenException("Invalid Token"));
        }
        
    }

    public Mono<String> getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return Mono.just(bearerToken.substring(TOKEN_PREFIX.length()));
        }
        return Mono.error(new InvalidTokenException("Token Missing"));
    }
}
