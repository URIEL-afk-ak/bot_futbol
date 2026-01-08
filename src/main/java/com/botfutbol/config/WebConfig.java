package com.botfutbol.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos de audio desde el directorio uploads/audio
        registry.addResourceHandler("/uploads/audio/**")
                .addResourceLocations("file:uploads/audio/");
    }
}
