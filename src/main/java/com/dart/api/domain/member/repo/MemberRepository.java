package com.dart.api.domain.member.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByNickname(String nickname);
	Optional<Member> findByEmail(String email);

}
