package luckyseven.dart.api.domain.member.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
