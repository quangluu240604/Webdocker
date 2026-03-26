package fit.hutech.spring.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "book")
public class Book {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", length = 50, nullable = false)
	@NotBlank(message = "Title is required")
	@Size(max = 50, message = "Title must be at most 50 characters")
	private String title;

	@Column(name = "author", length = 50, nullable = false)
	@NotBlank(message = "Author is required")
	@Size(max = 50, message = "Author must be at most 50 characters")
	private String author;

	@Column(name = "price")
	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
	private Double price;

	@Column(name = "image_url", length = 255)
	@Size(max = 255, message = "Image URL must be at most 255 characters")
	private String imageUrl;

	@Column(name = "description", length = 1000)
	@Size(max = 1000, message = "Description must be at most 1000 characters")
	private String description;

	@Column(name = "stock_quantity")
	@PositiveOrZero(message = "Stock must be greater than or equal to 0")
	private Integer stockQuantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", referencedColumnName = "id")
	@NotNull(message = "Category is required")
	@ToString.Exclude
	private Category category;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Book book = (Book) o;
		return getId() != null && Objects.equals(getId(), book.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
