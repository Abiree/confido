package com.confido.api.auth.services.impl;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpirationTime;

  private String buildToken(
      Map<String, Object> claims, UserDetails userDetails, long expirationTime) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(getKeyIn(), SignatureAlgorithm.HS256)
        .compact();
  }

  private Key getKeyIn() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(getKeyIn()).build().parseClaimsJws(token).getBody();
  }

  private <T> T getClaimsFromToken(String token, Function<Claims, T> claimResolver) {
    return claimResolver.apply(getAllClaimsFromToken(token));
  }

  public String extractEmail(String token) {
    return getClaimsFromToken(token, Claims::getSubject);
  }

  public Long getExpirationTime() {
    return jwtExpirationTime;
  }

  private Boolean isTokenExpired(String token) {
    return getClaimsFromToken(token, Claims::getExpiration).before(new Date());
  }

  public Boolean isTokenValid(String token, UserDetails userDetails) {
    String email = userDetails.getUsername();
    return email.equals(extractEmail(token)) && !isTokenExpired(token);
  }

  public String generateToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, jwtExpirationTime);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpirationTime);
  }
}
