package com.invoice.security;

import com.invoice.exception.AuthorizationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
        } else {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AuthorizationException(SecurityConstants.AUTHORIZATION_HEADER +" Bearer token is missing");
            }
        }
        final String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser().setSigningKey(SecurityConstants.JWT_SECRET_KEY).parseClaimsJws(token).getBody();
            request.setAttribute("claims", claims);
        }catch (JwtException | IllegalArgumentException e){
            throw new AuthorizationException(SecurityConstants.AUTHORIZATION_HEADER +" token is invalid or expired");
        }

        filterChain.doFilter(request, response);
    }
}