package com.joelcode.personalinvestmentportfoliotracker.jwt;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    // This class is responsible for creating, parsing, and validating JWT tokens.

    // Inject value from config for token signing
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Determines how long the JWT token is valid for.
    @Value("${app.jwtexpiration}")
    private int jwtExpirationInMs;

    // Generate jwt token for user
    public String generateToken(User user) {
        // Create expiry date
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        // Generate secret key
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        // Build token and sign
        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("password", user.getPassword())
                .claim("username", user.getUsername())
                .claim("roles", user.getRoles().name())
                .claim("fullName", user.getFullName())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    // Get user id from jwt token
    public UUID getUserIdFromToken(String token){
        Claims claim = getClaims(token);
        return UUID.fromString(claim.getSubject());
    }

    // Get username from jwt token
    public String getUsernameFromToken(String token){
        Claims claim = getClaims(token);
        return claim.get("username", String.class);
    }

    // Validate jwt token
    public boolean validateToken(String token) {
        try {
            getClaims(token); // If this doesn't throw, token is valid
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature: " + ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty: " + ex.getMessage());
        }
        return false;
    }

    // Validate jwt is not expired
    public boolean isTokenExpire(String token){
        try {
            Claims claim = getClaims(token);
            Date expiration = claim.getExpiration();
            return expiration.before(new Date(System.currentTimeMillis()));
        }
        catch (Exception e){
            return true;
        }
    }
}
