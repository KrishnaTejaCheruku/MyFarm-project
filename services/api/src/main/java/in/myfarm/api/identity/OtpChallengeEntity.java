package in.myfarm.api.identity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * A single OTP challenge issued for a phone number. Requesting a new
 * OTP for a phone that already has a live challenge supersedes it --
 * OtpService marks the old one consumed rather than leaving two live
 * codes outstanding for the same phone.
 */
@Entity
@Table(name = "identity_otp_challenge")
class OtpChallengeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 10, updatable = false)
	private String phone;

	// SHA-256 hex digest of the 6-digit code -- never store the code
	// itself, same reasoning as a password hash even though this is
	// short-lived.
	@Column(name = "code_hash", nullable = false, length = 64, updatable = false)
	private String codeHash;

	@Column(nullable = false)
	private int attempts;

	@Column(name = "expires_at", nullable = false, updatable = false)
	private LocalDateTime expiresAt;

	@Column(name = "consumed_at")
	private LocalDateTime consumedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected OtpChallengeEntity() {
	}

	OtpChallengeEntity(String phone, String codeHash, LocalDateTime expiresAt) {
		this.phone = phone;
		this.codeHash = codeHash;
		this.expiresAt = expiresAt;
		this.attempts = 0;
	}

	String phone() {
		return phone;
	}

	String codeHash() {
		return codeHash;
	}

	int attempts() {
		return attempts;
	}

	LocalDateTime consumedAt() {
		return consumedAt;
	}

	boolean isConsumed() {
		return consumedAt != null;
	}

	boolean isExpired(LocalDateTime now) {
		return now.isAfter(expiresAt);
	}

	void registerFailedAttempt() {
		attempts++;
	}

	void markConsumed(LocalDateTime now) {
		consumedAt = now;
	}
}
