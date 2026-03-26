package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {
	List<Invoice> findByUserUsernameOrderByIdDesc(String username);

	Optional<Invoice> findByIdAndUserUsername(Long id, String username);
}
