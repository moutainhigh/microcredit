package banger.framework.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.stereotype.Component;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;




import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.Key;
import java.util.Date;

/**
 * Created by ThinkPad on 2017/6/17.
 */
@Component
public class JwtUtil {
    public static String sercetKey="http://www.baihang-china.com/";
    public final static long keeptime = 3600000*12;
    public final static long skewSeconds = 3600000*24*7;

    public static String generToken(String id, String issuer, String subject){
        long ttlMillis = keeptime;
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(sercetKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now);
        if(subject!=null){
            builder.setSubject(subject);
        }
        if(issuer!=null){
            builder.setIssuer(issuer);
        }
        builder.signWith(signatureAlgorithm, signingKey);

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    public static String updateToken(String token){
        try {
            Claims claims=verifyToken(token);
            String id=claims.getId();
            String subject=claims.getSubject();
            String issuer=claims.getIssuer();
            Date date = claims.getExpiration();
            return generToken(id, issuer, subject);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "0";
    }


    public String updateTokenBase64Code(String token)  {
        BASE64Encoder base64Encoder=new  BASE64Encoder();
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            token=new String(decoder.decodeBuffer(token),"utf-8");
            Claims claims=verifyToken(token);
            String id=claims.getId();
            String subject=claims.getSubject();
            String issuer=claims.getIssuer();
            Date date = claims.getExpiration();
            String newToken = generToken(id, issuer, subject);
            return base64Encoder.encode(newToken.getBytes());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "0";
    }


    public static Claims verifyToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(sercetKey))
                .setAllowedClockSkewSeconds(skewSeconds)
                .parseClaimsJws(token).getBody();
        return  claims;
    }
    
    public static void main(String[] args) {
    	String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI3IiwiaWF0IjoxNTI0OTA1NzkyLCJzdWIiOiJtaWNybyIsImlzcyI6ImJhbmdlciIsImV4cCI6MTUyNDkwOTM5Mn0.cCEnK1txFN7DSaRqpCMzVV2hcxICNna576whlRJnP2w";
    	Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(sercetKey))
                .setAllowedClockSkewSeconds(skewSeconds)
                .parseClaimsJws(token).getBody();
    	
    }

}