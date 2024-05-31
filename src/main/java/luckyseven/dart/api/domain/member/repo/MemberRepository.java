package luckyseven.dart.api.domain.member.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.auth.OAuthProvider;
import luckyseven.dart.api.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);

}
