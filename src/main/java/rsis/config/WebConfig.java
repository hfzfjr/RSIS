package rsis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    /**
     * Configure resource handlers for serving uploaded files.
     * This method is called automatically by Spring MVC during application startup.
     * 
     * @param registry the ResourceHandlerRegistry to configure
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Serve uploaded files from the configured upload path
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * Pastikan file manifest.webmanifest dilayani dengan Content-Type
     * application/manifest+json agar dikenali browser saat "Add to Home Screen".
     */
    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> webmanifestMimeCustomizer() {
        return factory -> factory.addMimeMappings(
                mappings -> mappings.add("webmanifest", "application/manifest+json"));
    }
}
