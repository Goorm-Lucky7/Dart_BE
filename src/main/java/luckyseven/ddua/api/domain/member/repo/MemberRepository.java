package luckyseven.ddua.api.domain.member.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.ddua.api.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
