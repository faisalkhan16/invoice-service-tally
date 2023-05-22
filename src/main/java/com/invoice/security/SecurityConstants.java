package com.invoice.security;

import java.util.Base64;

public class SecurityConstants {

    public static String AUTHORIZATION_HEADER = "Authorization";

    public static String JWT_SECRET_KEY = Base64.getEncoder().encodeToString("FkMuhkHan16".getBytes());
}
