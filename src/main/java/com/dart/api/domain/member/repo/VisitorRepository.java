package com.dart.api.domain.member.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.member.entity.Visitor;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
}
