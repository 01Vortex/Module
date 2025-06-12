package com.example.login.util;




import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private static final long EXPIRATION = 86400000; // 24小时

       public static String generateToken(String username) {
           return Jwts.builder()
               .setSubject(username)
               .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
               .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
               .compact();
       }

       public static String parseToken(String token) {
           return Jwts.parser()
               .setSigningKey(SECRET_KEY)
               .parseClaimsJws(token)
               .getBody()
               .getSubject();
       }
   }
   