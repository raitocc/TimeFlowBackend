package org.whu.timeflow.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtils {

    // 1. 密钥 (随便写，长一点，严禁泄露)
    private static final String SECRET = "TimeFlow_WHU_Project_Secret_Key_For_Jwt_Signing_2025";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 2. 过期时间 (这里设为 7 天，单位毫秒)
    private static final long EXPIRE = 604800000L;

    /**
     * 生成 Token
     */
    public static String createToken(String userId, String email) {
        return Jwts.builder()
                .setSubject(userId)           // 存 ID
                .claim("email", email)        // 存 邮箱
                .setIssuedAt(new Date())      // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE)) // 过期时间
                .signWith(KEY, SignatureAlgorithm.HS256) // 签名
                .compact();
    }

    /**
     * 解析 Token，返回 Claims (包含 payload 信息)
     * 如果解析失败（过期、篡改）会抛出异常
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}