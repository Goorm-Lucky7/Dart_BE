package luckyseven.ddua.api.domain.payment.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import luckyseven.ddua.api.domain.member.entity.Member;
import luckyseven.ddua.api.global.common.BaseTimeEntity;

@Entity
@Getter
@Table(name = "tbl_payment_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentInfo {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "amount")
	private BigDecimal amount;

	@Column(name = "imp_uid")
	private String impUid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private PaymentInfo(
		BigDecimal amount,
		String impUid
	) {
		this.amount = amount;
		this.impUid = impUid;
	}
}
