package com.dart.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dart.global.auth.handler.AuthUserArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	public static final String[] ALLOWED_ORIGINS = {
		"http://localhost:5173",
		"https://dartgallery.site"
	};

	public static final String[] ALLOWED_METHODS = {
		"GET", "POST", "PUT", "DELETE", "OPTIONS"
	};

	public static final String CORS_MAPPING_PATH = "/api/**";
	public static final String ALLOWED_HEADERS = "*";

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new AuthUserArgumentResolver());
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping(CORS_MAPPING_PATH)
			.allowedOrigins(ALLOWED_ORIGINS)
			.allowedMethods(ALLOWED_METHODS)
			.allowedHeaders(ALLOWED_HEADERS)
			.allowCredentials(true);
	}
}
