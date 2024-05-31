package luckyseven.dart.api.domain.auth;

public record AuthUser(
	String email,
	String nickname
) {

	public static AuthUser create(String email, String nickname) {
		return new AuthUser(email, nickname);
	}
}
