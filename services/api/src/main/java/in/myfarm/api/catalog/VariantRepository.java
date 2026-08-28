package in.myfarm.api.catalog;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface VariantRepository extends JpaRepository<VariantEntity, Long> {

	@Query("""
			select variant
			from VariantEntity variant
			where variant.product.id in :productIds
			  and variant.active = true
			order by variant.product.id, variant.sortOrder, variant.id
			""")
	List<VariantEntity> findActiveByProductIds(
			@Param("productIds") Collection<Long> productIds);
}
