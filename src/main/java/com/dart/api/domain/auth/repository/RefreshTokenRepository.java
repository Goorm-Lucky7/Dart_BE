package com.dart.api.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.auth.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Boolean existsByEmail(String email);
	Optional<RefreshToken> findByToken(String token);
	void deleteByEmail(String email);
}
