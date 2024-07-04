package com.dart.api.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByEmail(String email);
	boolean existsByNickname(String nickname);
	Optional<Member> findByEmail(String email);
	Optional<Member> findByNickname(String nickname);
	List<Member> findByEmailIn(List<String> emailList);

}
