package com.confido.api.auth.services.impl;

import java.security.Key;
import java.time.LocalDateTime;
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

  @Value("${security.access-jwt.expiration-time}")
  private long accessJwtExpirationTime;

  @Value("${security.refresh-jwt.expiration-time}")
  private long refreshJwtExpirationTime;

  private String buildToken(
      Map<String, Object> claims, UserDetails userDetails, long expirationTime) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000))
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

  public LocalDateTime getAccessJwtExpirationTime() {
    return LocalDateTime.now().plusSeconds(accessJwtExpirationTime);
  }

  public LocalDateTime getRefreshJwtExpirationTime() {
    return LocalDateTime.now().plusSeconds(refreshJwtExpirationTime);
  }

  public Boolean isTokenExpired(LocalDateTime tokenExpirationTime) {
    return tokenExpirationTime.isBefore(LocalDateTime.now());
  }

  public Boolean isRefreshTokenValid(
      String storedToken, String tokenFromRequest, LocalDateTime expirationTime) {
    return tokenFromRequest.equals(storedToken) && !isTokenExpired(expirationTime);
  }

  private Boolean isTokenExpired(String token) {
    return getClaimsFromToken(token, Claims::getExpiration).before(new Date());
  }

  public Boolean isTokenValid(String token, UserDetails userDetails) {
    String email = userDetails.getUsername();
    return email.equals(extractEmail(token)) && !isTokenExpired(token);
  }

  public String generateAccessToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, accessJwtExpirationTime);
  }

  public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, accessJwtExpirationTime);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshJwtExpirationTime);
  }

  public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, refreshJwtExpirationTime);
  }
}
