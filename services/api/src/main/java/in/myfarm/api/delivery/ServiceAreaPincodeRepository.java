package in.myfarm.api.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ServiceAreaPincodeRepository
		extends JpaRepository<ServiceAreaPincodeEntity, Long> {

	@Query("""
			select count(mapping)
			from ServiceAreaPincodeEntity mapping
			where mapping.serviceArea.code = :areaCode
			  and mapping.serviceArea.active = true
			  and mapping.pincode = :pincode
			  and mapping.active = true
			""")
	long countActiveMappings(
			@Param("areaCode") String areaCode,
			@Param("pincode") String pincode);
}
