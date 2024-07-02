package com.dart.global.config;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.global.auth.filter.AuthenticationFilter;
import com.dart.global.auth.filter.CustomFilter;
import com.dart.global.auth.handler.CustomAuthenticationEntryPoint;
import com.dart.global.common.util.CookieUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CookieUtil cookieUtil;
	private final JwtProviderService jwtProviderService;
	private final HandlerExceptionResolver handlerExceptionResolver;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	public static final String[] ALLOWED_ORIGINS = {
		"http://localhost:5173",
		"https://dartgallery.site",
		"https://www.dartgallery.site"
	};

	public static final String[] ALLOWED_METHODS = {
		"GET", "POST", "PUT", "DELETE", "OPTIONS"
	};

	public static final String CORS_MAPPING_PATH = "/api/**";
	public static final String[] ALLOWED_HEADERS = {
		"Authorization", "Content-Type", "X-Requested-With",
		"Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
	};

	public static final String[] EXPOSED_HEADERS = {
		"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Custom-Header"
	};

	public SecurityConfig(
		CookieUtil cookieUtil, JwtProviderService jwtProviderService,
		@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
		CustomAuthenticationEntryPoint customAuthenticationEntryPoint
	) {
		this.cookieUtil = cookieUtil;
		this.jwtProviderService = jwtProviderService;
		this.handlerExceptionResolver = handlerExceptionResolver;
		this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
			.requestMatchers("/h2-console/**")
			.requestMatchers("/api/signup/**")
			.requestMatchers("/api/email/**")
			.requestMatchers("/api/nickname/check")
			.requestMatchers("/api/payment/kakao/**")
			.requestMatchers("/api/login")
			.requestMatchers("/api/reissue");
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(ALLOWED_ORIGINS));
		configuration.setAllowedMethods(List.of(ALLOWED_METHODS));
		configuration.setAllowedHeaders(List.of(ALLOWED_HEADERS));
		configuration.setExposedHeaders(List.of(EXPOSED_HEADERS));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		httpSecurity
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

		httpSecurity
			.authorizeHttpRequests((auth) -> auth
			.requestMatchers("/favicon.ico").permitAll()
			.requestMatchers("/ws/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/signup/*").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/login/oauth2/*").permitAll()
			.requestMatchers(HttpMethod.POST, "/api/email/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/galleries/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/galleries/info").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/reviews/{gallery-id}/**").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/mypage").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/members").permitAll()
			.requestMatchers(HttpMethod.GET, "/api/reviews/info").permitAll()
			.anyRequest().authenticated()
		);

		httpSecurity
			.addFilterBefore(new CorsFilter(corsConfigurationSource()), ChannelProcessingFilter.class)
			.addFilterBefore(new CustomFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new AuthenticationFilter(cookieUtil, jwtProviderService, handlerExceptionResolver),
				UsernamePasswordAuthenticationFilter.class);

		httpSecurity.exceptionHandling((exceptionHandling) -> {
			exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint);
		});

		return httpSecurity.build();
	}
}
