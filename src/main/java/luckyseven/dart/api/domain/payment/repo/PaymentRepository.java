package luckyseven.dart.api.domain.payment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
