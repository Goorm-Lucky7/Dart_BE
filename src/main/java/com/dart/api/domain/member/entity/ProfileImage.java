package com.dart.api.domain.member.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import luckyseven.dart.api.dto.member.request.MemberUpdateDto;

@Entity
@Getter
@Table(name = "tbl_profile_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "profile_image")
	private String profileImage;

	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	public ProfileImage(String profileImage, Member member) {
		this.profileImage = profileImage;
		this.member = member;
	}

	public static ProfileImage createDefaultForMember(Member member) {
		return ProfileImage.builder()
			.profileImage(null)
			.member(member)
			.build();
	}

	public void updateProfileImage(MemberUpdateDto memberUpdateDto) {
		this.profileImage = memberUpdateDto.profileImage();
	}
}
