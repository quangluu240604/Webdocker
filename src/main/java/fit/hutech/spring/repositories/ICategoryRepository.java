package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByName(String name);
}
