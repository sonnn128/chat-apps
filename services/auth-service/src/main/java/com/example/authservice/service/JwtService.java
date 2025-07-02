package com.example.authservice.service;

import com.example.authservice.dto.IntrospectResponse;
import com.example.authservice.dto.response.UserAuthDetailResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

import io.jsonwebtoken.Claims;

import java.util.function.Function;

@Service
public class JwtService {
    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserAuthDetailResponse user,
            int timeToLive
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + timeToLive))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //    public String generateToken(UserAuthDetailResponse user, int timeToLive) {
//        return generateToken(new HashMap<>(), user, timeToLive);
//    }
    public String generateToken(UserAuthDetailResponse user, int timeToLive) {
        // Tạo một map để chứa các thông tin thêm (extra claims)
        Map<String, Object> extraClaims = new HashMap<>();

        // Thêm userId và roles vào claims
        // .toString() để đảm bảo kiểu dữ liệu là String, dễ xử lý hơn khi đọc ra
        extraClaims.put("userId", user.getId().toString());

        // Chuyển Role enum thành String
        // Giả sử user.getRole() trả về một enum
        if (user.getRole() != null) {
            extraClaims.put("roles", List.of(user.getRole().name()));
        } else {
            extraClaims.put("roles", Collections.emptyList());
        }

        // Gọi phương thức generateToken gốc với extraClaims
        return generateToken(extraClaims, user, timeToLive);
    }

    public boolean isTokenValid(String token, UserAuthDetailResponse user) {
        String username = extractUsername(token);
        return username.equals(user.getEmail()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public IntrospectResponse introspectToken(String token) {
        Claims claims = this.extractAllClaims(token);

        String username = claims.getSubject();

        String userIdStr = claims.get("userId", String.class);
        UUID userId = (userIdStr != null) ? UUID.fromString(userIdStr) : null;

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
            roles = Collections.emptyList();
        }

        return new IntrospectResponse(true, username, userId, roles);
    }
}
