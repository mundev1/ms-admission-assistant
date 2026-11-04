package com.admissions.engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global Spring MVC configuration.
 *
 * <p>Configures Cross-Origin Resource Sharing (CORS) to allow all origins,
 * enabling frontend applications (React, Vue, etc.) running on different ports
 * to communicate with this backend during development.</p>
 *
 * <p><strong>Note:</strong> For production deployments, restrict {@code allowedOrigins}
 * to the actual frontend domain(s) to prevent unauthorized cross-origin access.</p>
 */
@Configuration
public class WebConfig {

    /**
     * Produces a {@link WebMvcConfigurer} bean that registers a global CORS mapping.
     *
     * <p>All endpoints ({@code /**}) accept requests from any origin with
     * GET, POST, PUT, and DELETE HTTP methods and any request headers.</p>
     *
     * @return a {@link WebMvcConfigurer} with global CORS settings applied
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry registry) {
                registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
                registry.addViewController("/ects-screener").setViewName("forward:/ects-screener.html");
                registry.addViewController("/scholarships").setViewName("forward:/scholarships.html");
                registry.addViewController("/faculty-critic").setViewName("forward:/faculty-critic.html");
                registry.addViewController("/timeline").setViewName("forward:/timeline.html");
            }
        };
    }
}
