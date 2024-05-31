package luckyseven.dart.api.domain.member.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.member.entity.ProfileImage;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

	Optional<ProfileImage> findByMemberEmail(String email);

	default String findByMemberId(Long memberId) {
		ProfileImage profileImage = findById(memberId).orElse(null);
		return profileImage != null ? profileImage.getProfileImage() : null;
	}
}
