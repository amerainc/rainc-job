package com.rainc.job.util;

import cn.hutool.core.codec.Base64;
import com.rainc.job.config.JwtProperties;
import com.rainc.job.model.JobUserDO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * @Author rainc
 * @create 2020/12/24 12:47
 */
@Log4j2
@Data
@Component
public class JwtUtils implements InitializingBean {
    @Resource
    JwtProperties jwtProperties;
    /**
     * 秘钥
     */
    private Key signKey;
    /**
     * 签名算法
     */
    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    /**
     * 生成jwt token
     */
    public String generateToken(JobUserDO jobUserDO) {
        //发布时间
        Date nowDate = new Date();
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + jwtProperties.getExpire() * 1000);

        return Jwts.builder()
                //设置头部
                .setHeaderParam("typ", "JWT")
                //设置参数
                .claim("username", jobUserDO.getUsername())
                .claim("password", jobUserDO.getPassword())
                //设置生效和失效时间
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(signKey, signatureAlgorithm)
                .compact();
    }

    public Claims getClaimByToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //生成秘钥
        byte[] keyBytes = DatatypeConverter.parseBase64Binary(Base64.encode(jwtProperties.getSecret()));
        this.signKey = new SecretKeySpec(keyBytes, signatureAlgorithm.getJcaName());
    }
}
