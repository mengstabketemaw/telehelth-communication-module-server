package io.telehelth.communication.service;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Service
public class Service {
  private final String API_KEY = "9ec65ad7-b2f2-4f6c-90b2-55a955ae4ef9";
  private final String SECRET_KEY = "96c81e155e95e5f6aeb8c0f90ade80993d607c16e4cff37ea6202a3b55ac7d70";

    private String generateManagementToken() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("apikey", API_KEY);
        payload.put("permissions", new String[]{"allow_join"});
        return  Jwts.builder().setClaims(payload).setId(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() + 86400 * 1000))
                .setIssuedAt(Date.from(Instant.ofEpochMilli(System.currentTimeMillis() - 60000)))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes()).compact();
    }

    public String getToken(){
        return generateManagementToken();
    }
}
