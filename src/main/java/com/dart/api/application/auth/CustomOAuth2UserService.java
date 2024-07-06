package com.dart.api.application.auth;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.dart.api.domain.auth.entity.CustomOAuth2User;
import com.dart.api.domain.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final OAuthLoginService oauthLoginService;
	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

	private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		try {
			OAuth2User oauth2User = delegate.loadUser(userRequest);
			String oauthProvider = userRequest.getClientRegistration().getRegistrationId();

			Map<String, Object> loginResponse = oauthLoginService.socialLogin(oauth2User, oauthProvider);
			boolean isNewUser = (Boolean) loginResponse.get("isNewUser");
			Member member = (Member) loginResponse.get("member");

			return new CustomOAuth2User(oauth2User, oauthProvider, member, isNewUser);
		} catch (Exception e) {
			throw e;
		}
	}
}