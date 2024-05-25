package luckyseven.ddua.api.domain.payment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.ddua.api.domain.payment.entity.PaymentInfo;

public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
}
