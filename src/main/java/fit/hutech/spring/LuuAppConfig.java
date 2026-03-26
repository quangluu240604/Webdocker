package fit.hutech.spring;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.Category;
import fit.hutech.spring.entities.Role;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.BookRepository;
import fit.hutech.spring.repositories.ICategoryRepository;
import fit.hutech.spring.repositories.IRoleRepository;
import fit.hutech.spring.repositories.IUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class LuuAppConfig {
	@Bean
	public CommandLineRunner seedBooks(BookRepository bookRepository, ICategoryRepository categoryRepository) {
		return args -> {
			if (bookRepository.count() == 0) {
				var defaultCategory = categoryRepository.findByName("General")
						.orElseGet(() -> categoryRepository.save(Category.builder().name("General").build()));
				bookRepository.save(Book.builder().title("Lap trinh Web Spring Framework").author("Anh Nguyen").price(29.99).stockQuantity(10).category(defaultCategory).build());
				bookRepository.save(Book.builder().title("Lap trinh ung dung Java").author("Huy Cuong").price(45.63).stockQuantity(15).category(defaultCategory).build());
				bookRepository.save(Book.builder().title("Lap trinh Web Spring Boot").author("Xuan Nhan").price(12.0).stockQuantity(20).category(defaultCategory).build());
				bookRepository.save(Book.builder().title("Lap trinh Web Spring MVC").author("Anh Nguyen").price(0.12).stockQuantity(30).category(defaultCategory).build());
			}
		};
	}

	@Bean
	public CommandLineRunner seedRoles(IRoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				roleRepository.save(Role.builder().name("USER").description("Default user role").build());
			}
			if (roleRepository.findByName("ADMIN").isEmpty()) {
				roleRepository.save(Role.builder().name("ADMIN").description("Administrator role").build());
			}
		};
	}

	@Bean
	public CommandLineRunner seedMissingUserRoles(IUserRepository userRepository, IRoleRepository roleRepository) {
		return args -> {
			var userRole = roleRepository.findByName("USER").orElse(null);
			if (userRole == null) {
				return;
			}
			for (User user : userRepository.findAll()) {
				if (user.getRoles() == null || user.getRoles().isEmpty()) {
					user.getRoles().add(userRole);
					userRepository.save(user);
				}
			}
		};
	}

	@Bean
	public CommandLineRunner migrateInvoiceColumns(JdbcTemplate jdbcTemplate) {
		return args -> {
			ensureColumn(jdbcTemplate, "invoice", "receiver_name", "ALTER TABLE invoice ADD COLUMN receiver_name VARCHAR(100)");
			ensureColumn(jdbcTemplate, "invoice", "receiver_phone", "ALTER TABLE invoice ADD COLUMN receiver_phone VARCHAR(20)");
			ensureColumn(jdbcTemplate, "invoice", "shipping_address", "ALTER TABLE invoice ADD COLUMN shipping_address VARCHAR(255)");
			ensureColumn(jdbcTemplate, "invoice", "note", "ALTER TABLE invoice ADD COLUMN note VARCHAR(500)");
			ensureColumn(jdbcTemplate, "invoice", "status", "ALTER TABLE invoice ADD COLUMN status VARCHAR(20)");
			ensureColumn(jdbcTemplate, "invoice", "total_amount", "ALTER TABLE invoice ADD COLUMN total_amount DOUBLE");
			ensureColumn(jdbcTemplate, "invoice", "created_at", "ALTER TABLE invoice ADD COLUMN created_at DATETIME");
		};
	}

	private void ensureColumn(JdbcTemplate jdbcTemplate, String tableName, String columnName, String alterSql) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
				Integer.class,
				tableName,
				columnName
		);
		if (count != null && count == 0) {
			jdbcTemplate.execute(alterSql);
		}
	}
}
