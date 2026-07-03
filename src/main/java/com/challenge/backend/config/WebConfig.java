package com.challenge.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Libera todos os endpoints
                .allowedOrigins(
                        "http://localhost:4200", 
                        "http://localhost:3000",
                        "http://localhost:5173", // Vite
                        "https://frontend-solicitations-4zht.vercel.app" // Vercel
                ) 
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
