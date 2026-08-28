package in.myfarm.api.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OtpService {

	private static final Logger log = LoggerFactory.getLogger(OtpService.class);

	private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);
	private static final int MAX_ATTEMPTS = 5;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final OtpChallengeRepository challengeRepository;
	private final Clock clock;

	OtpService(OtpChallengeRepository challengeRepository, Clock clock) {
		this.challengeRepository = challengeRepository;
		this.clock = clock;
	}

	@Transactional
	String requestOtp(String phone) {
		// Supersede any still-live challenge for this phone so a phone
		// never has two valid codes outstanding at once.
		challengeRepository
				.findFirstByPhoneAndConsumedAtIsNullOrderByIdDesc(phone)
				.ifPresent(existing -> existing.markConsumed(now()));

		String code = generateCode();
		OtpChallengeEntity challenge = new OtpChallengeEntity(
				phone, hash(code), now().plus(CHALLENGE_TTL));
		challengeRepository.save(challenge);

		// No SMS gateway integrated yet -- that needs a real
		// account/credentials only Krishna can set up. Logged here so
		// the flow is actually usable in local/dev; AuthController only
		// echoes the code back to the caller when
		// myfarm.otp.expose-in-response is true.
		log.info("OTP for {}: {} (SMS gateway not integrated -- "
				+ "logged for local/dev use only)", phone, code);

		return code;
	}

	// noRollbackFor: a wrong attempt must still increment and persist
	// OtpChallengeEntity.attempts even though the method always throws
	// on that path -- otherwise Spring's default rollback-on-RuntimeException
	// undoes the increment and lockout never actually engages.
	@Transactional(noRollbackFor = InvalidOtpException.class)
	void verifyOtp(String phone, String code) {
		OtpChallengeEntity challenge = challengeRepository
				.findFirstByPhoneAndConsumedAtIsNullOrderByIdDesc(phone)
				.orElseThrow(InvalidOtpException::new);

		if (challenge.isExpired(now()) || challenge.attempts() >= MAX_ATTEMPTS) {
			throw new InvalidOtpException();
		}
		if (!challenge.codeHash().equals(hash(code))) {
			challenge.registerFailedAttempt();
			throw new InvalidOtpException();
		}
		challenge.markConsumed(now());
	}

	private LocalDateTime now() {
		return LocalDateTime.now(clock);
	}

	private static String generateCode() {
		return String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
	}

	private static String hash(String code) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(code.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(bytes);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
