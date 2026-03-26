package fit.hutech.spring.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoice")
public class Invoice {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@ToString.Exclude
	private User user;

	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<InvoiceItem> invoiceItems = new ArrayList<>();

	@Column(name = "receiver_name", length = 100)
	private String receiverName;

	@Column(name = "receiver_phone", length = 20)
	private String receiverPhone;

	@Column(name = "shipping_address", length = 255)
	private String shippingAddress;

	@Column(name = "note", length = 500)
	private String note;

	@Column(name = "status", length = 20)
	private String status;

	@Column(name = "total_amount")
	private Double totalAmount;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Invoice invoice = (Invoice) o;
		return getId() != null && Objects.equals(getId(), invoice.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
