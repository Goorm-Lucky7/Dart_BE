package luckyseven.dart.api.application.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;;
import luckyseven.dart.api.domain.auth.AuthUser;
import luckyseven.dart.api.domain.member.entity.ProfileImage;
import luckyseven.dart.api.domain.member.entity.Member;
import luckyseven.dart.api.domain.member.repo.MemberRepository;
import luckyseven.dart.api.domain.member.repo.ProfileImageRepository;
import luckyseven.dart.api.dto.member.request.MemberUpdateDto;
import luckyseven.dart.api.dto.member.request.SignUpDto;
import luckyseven.dart.api.dto.member.response.MemberProfileResDto;
import luckyseven.dart.global.error.exception.ConflictException;
import luckyseven.dart.global.error.exception.NotFoundException;
import luckyseven.dart.global.error.model.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final ProfileImageRepository profileImageRepository;

	@Transactional
	public void signUp(SignUpDto signUpDto) {
		final String encodedPassword = passwordEncoder.encode(signUpDto.password());
		final Member member = Member.signup(signUpDto, encodedPassword);
		final ProfileImage profileImage = ProfileImage.createDefaultForMember(member);

		memberRepository.save(member);
		profileImageRepository.save(profileImage);
	}

	public MemberProfileResDto getMemberProfile(AuthUser authUser) {
		final Member member = findMemberByEmail(authUser.email());
		final ProfileImage profileImage = findProfileImageByEmail(authUser.email());

		return convertToMemberProfileResDto(member, profileImage);
	}

	@Transactional
	public void updateMemberProfile(AuthUser authUser, MemberUpdateDto memberUpdateDto) {
		final Member member = findMemberByEmail(authUser.email());
		final ProfileImage profileImage = findProfileImageByEmail(authUser.email());
		final String encodedPassword = passwordEncoder.encode(memberUpdateDto.password());

		validateNicknameForUpdate(memberUpdateDto.nickname());
		member.updateMemberProfile(memberUpdateDto, encodedPassword);
		profileImage.updateProfileImage(memberUpdateDto);
	}

	private Member findMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private ProfileImage findProfileImageByEmail(String email) {
		return profileImageRepository.findByMemberEmail(email)
			.orElseThrow(()->new NotFoundException(ErrorCode.FAIL_IMAGE_NOT_FOUND));
	}

	private MemberProfileResDto convertToMemberProfileResDto(Member member, ProfileImage profileImage) {
		return new MemberProfileResDto(member.getEmail(), member.getNickname(), member.getIntroduce(),
			member.getBank(), member.getAccount(), profileImage.getProfileImage());
	}

	private void validateNickname(String nickname) {
		if(memberRepository.existsByNickname(nickname)){
			throw new ConflictException(ErrorCode.FAIL_NICKNAME_CONFLICT);
		}
	}

	private void validateNicknameForUpdate(String nickname) {
		if(nickname != null && !nickname.trim().isEmpty()) {
			validateNickname(nickname);
		}
	}

}