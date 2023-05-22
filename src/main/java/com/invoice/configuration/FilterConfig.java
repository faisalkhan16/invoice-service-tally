package com.invoice.configuration;

import com.invoice.security.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean jwtFilter() {
        FilterRegistrationBean filter = new FilterRegistrationBean();
        filter.setFilter(new JwtFilter());

        filter.addUrlPatterns("/seller","/embedxml","/archive","/email","/issue","/report","/pending","/retry","/invoice","/import","/issuestandard","/issuesimplified");
        return filter;
    }
}