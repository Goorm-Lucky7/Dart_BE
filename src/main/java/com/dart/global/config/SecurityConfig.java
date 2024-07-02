package com.dart.global.config;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
			.addFilterBefore(new CustomFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new AuthenticationFilter(cookieUtil, jwtProviderService, handlerExceptionResolver),
				UsernamePasswordAuthenticationFilter.class);

		httpSecurity.exceptionHandling((exceptionHandling) -> {
			exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint);
		});

		return httpSecurity.build();
	}
}
