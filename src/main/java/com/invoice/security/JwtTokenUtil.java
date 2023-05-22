package com.invoice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

	public String getUsernameFromToken(String token) {

		String subject = getClaimFromToken(token, Claims::getSubject);
		return  subject.split(";")[0];
	}

	public String getVatNumberFromToken(String token) {

		String subject = getClaimFromToken(token, Claims::getSubject);
		return  subject.split(";")[1];
	}

	public String getEgsSerialNumberFromToken(String token) {

		String subject = getClaimFromToken(token, Claims::getSubject);
		return  subject.split(";")[2];
	}
	public Date getIssuedAtDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getIssuedAt);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(SecurityConstants.JWT_SECRET_KEY).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public Boolean validateToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(SecurityConstants.JWT_SECRET_KEY).parseClaimsJws(token).getBody();
		return (!isTokenExpired(token));
	}

	public String getTokenFromAuthHeader(String authHeader){
		String token = authHeader.substring(7);
		return token;
	}

}
