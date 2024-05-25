package luckyseven.ddua.api.domain.chat.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;

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

@Entity
@Getter
@Table(name = "tbl_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "send_time", nullable = false)
	private LocalDateTime sendTime;

	@Column(name = "is_blinded", nullable = false)
	@ColumnDefault("false")
	private boolean isBlinded;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatroom_id")
	private Chatroom chatroom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private Message(
		String content,
		LocalDateTime sendTime,
		boolean isBlinded
	) {
		this.content = content;
		this.sendTime = sendTime;
		this.isBlinded = isBlinded;
	}
}
