package in.myfarm.api.catalog;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<ProductEntity, Long> {

	@EntityGraph(attributePaths = "category")
	Page<ProductEntity> findByActiveTrue(Pageable pageable);

	@EntityGraph(attributePaths = "category")
	Page<ProductEntity> findByActiveTrueAndCategory_CodeIgnoreCase(
			String categoryCode, Pageable pageable);

	@EntityGraph(attributePaths = "category")
	Optional<ProductEntity> findBySlugAndActiveTrue(String slug);
}
