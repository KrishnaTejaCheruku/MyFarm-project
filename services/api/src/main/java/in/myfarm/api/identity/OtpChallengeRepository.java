package in.myfarm.api.identity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface OtpChallengeRepository extends JpaRepository<OtpChallengeEntity, Long> {

	Optional<OtpChallengeEntity> findFirstByPhoneAndConsumedAtIsNullOrderByIdDesc(
			String phone);
}
