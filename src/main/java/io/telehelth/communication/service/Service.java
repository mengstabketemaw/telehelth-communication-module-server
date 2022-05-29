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
  private final String API_KEY = "cc11048d-f38d-4c8f-8e97-877c4b2964b1";
  private final String SECRET_KEY = "b2068b38aa78ac5d10e3e1696f972a6c59d02d48c040faa4d38d672a1c641cf7";

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
