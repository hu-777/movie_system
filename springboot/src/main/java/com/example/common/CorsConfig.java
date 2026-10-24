package com.example.common;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.*;
import java.util.Arrays;
@Configuration
public class CorsConfig {
    @Bean public CorsConfigurationSource corsConfigurationSource(@Value("${app.allowed-origins:http://127.0.0.1:5173,http://localhost:5173}") String origins){
        CorsConfiguration config=new CorsConfiguration();config.setAllowedOrigins(Arrays.asList(origins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Content-Type","X-CSRF-TOKEN"));config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",config);return source;
    }
}
