package com.semicolon.spring.security.jwt;

import com.semicolon.spring.exception.InvalidTokenException;
import com.semicolon.spring.security.jwt.auth.AuthDetails;
import com.semicolon.spring.security.jwt.auth.AuthDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.prefix}")
    private String prefix;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.exp.access}")
    private Long accessTokenExpiration;

    private final AuthDetailsService authDetailsService;

    public String generateAccessToken(Integer id){
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .setHeaderParam("typ", "JWT")
                .setSubject(id.toString())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .compact();
    }

    public String resolveToken(HttpServletRequest request){
        String bearer = request.getHeader(header);
        if(bearer!=null && bearer.startsWith(prefix)){
            if(bearer.length()>7){
                return bearer.substring(7);
            }else throw new InvalidTokenException();
        }
        return null;
    }

    public boolean validateToken(String token){
        try{
            return getTokenBody(token).getExpiration()
                    .after(new Date());
        }catch (Exception e){
            throw new InvalidTokenException();
        }
    }

    public Authentication authentication(String token){
        var authDetails = authDetailsService.loadUserByUsername(getId(token));
        return new UsernamePasswordAuthenticationToken(authDetails, "", authDetails.getAuthorities());
    }

    private String getId(String token){
        try{
            return getTokenBody(token).getSubject();
        }catch (Exception e){
            throw new InvalidTokenException();
        }
    }

    private String getSigningKey() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    private Claims getTokenBody(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey()).parseClaimsJws(token).getBody();
    }

}
