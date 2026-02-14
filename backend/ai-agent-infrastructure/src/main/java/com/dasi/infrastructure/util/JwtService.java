package com.dasi.infrastructure.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dasi.domain.util.jwt.JwtProperties;
import com.dasi.domain.auth.model.vo.UserVO;
import com.dasi.domain.util.jwt.IJwtService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class JwtService implements IJwtService {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";

    @Resource
    private JwtProperties jwtProperties;

    @Override
    public String generateToken(UserVO userVO) {
        if (userVO == null || userVO.getId() == null) {
            throw new IllegalArgumentException("用户信息缺失，无法签发 Token");
        }

        Date now = new Date();
        long expireMillis = jwtProperties.getExpireSeconds() * 1000;
        Date expireAt = new Date(now.getTime() + expireMillis);

        return JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withIssuedAt(now)
                .withExpiresAt(expireAt)
                .withClaim(CLAIM_USER_ID, userVO.getId())
                .withClaim(CLAIM_USERNAME, userVO.getUsername())
                .withClaim(CLAIM_ROLE, userVO.getRole())
                .sign(getAlgorithm());
    }

    @Override
    public DecodedJWT verifyToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new JWTVerificationException("Token 缺失");
        }

        JWTVerifier verifier = JWT.require(getAlgorithm())
                .withIssuer(jwtProperties.getIssuer())
                .build();

        return verifier.verify(token);
    }

    @Override
    public UserVO parseToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        Long userId = jwt.getClaim(CLAIM_USER_ID).asLong();
        String username = jwt.getClaim(CLAIM_USERNAME).asString();
        String role = jwt.getClaim(CLAIM_ROLE).asString();
        if (userId == null || !StringUtils.hasText(username) || !StringUtils.hasText(role)) {
            throw new JWTVerificationException("Token 缺少必要用户信息");
        }
        return UserVO.builder()
                .id(userId)
                .username(username)
                .role(role)
                .build();
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(jwtProperties.getSecret());
    }
}
