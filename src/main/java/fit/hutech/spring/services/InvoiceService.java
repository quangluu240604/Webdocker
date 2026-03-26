package fit.hutech.spring.services;

import fit.hutech.spring.daos.Cart;
import fit.hutech.spring.entities.Invoice;
import fit.hutech.spring.entities.InvoiceItem;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.IBookRepository;
import fit.hutech.spring.repositories.IInvoiceRepository;
import fit.hutech.spring.viewmodels.CheckoutVm;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class InvoiceService {
	private final IInvoiceRepository invoiceRepository;
	private final IBookRepository bookRepository;

	public CheckoutResult checkout(@NotNull User user, @NotNull Cart cart, @NotNull CheckoutVm checkoutVm) {
		if (cart.getCartItems().isEmpty()) {
			return new CheckoutResult(false, "Cart is empty.", null);
		}

		var invoice = new Invoice();
		invoice.setUser(user);
		invoice.setReceiverName(checkoutVm.getReceiverName());
		invoice.setReceiverPhone(checkoutVm.getReceiverPhone());
		invoice.setShippingAddress(checkoutVm.getShippingAddress());
		invoice.setNote(checkoutVm.getNote());
		invoice.setStatus("PENDING");
		invoice.setCreatedAt(LocalDateTime.now());
		List<InvoiceItem> invoiceItems = new ArrayList<>();
		double totalAmount = 0;

		for (var cartItem : cart.getCartItems()) {
			if (cartItem.getQuantity() <= 0) {
				return new CheckoutResult(false, "Invalid quantity for item: " + cartItem.getBookName(), null);
			}

			var bookOptional = bookRepository.findById(cartItem.getBookId());
			if (bookOptional.isEmpty()) {
				return new CheckoutResult(false, "Book not found: ID " + cartItem.getBookId(), null);
			}

			var book = bookOptional.get();
			int stock = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
			if (cartItem.getQuantity() > stock) {
				return new CheckoutResult(false, "Not enough stock for: " + book.getTitle(), null);
			}

			book.setStockQuantity(stock - cartItem.getQuantity());
			bookRepository.save(book);

			InvoiceItem invoiceItem = new InvoiceItem();
			invoiceItem.setInvoice(invoice);
			invoiceItem.setBook(book);
			invoiceItem.setQuantity(cartItem.getQuantity());
			// Use DB price to avoid client-side tampering.
			invoiceItem.setPrice(book.getPrice());
			invoiceItems.add(invoiceItem);
			totalAmount += book.getPrice() * cartItem.getQuantity();
		}

		invoice.setTotalAmount(totalAmount);
		invoice.setInvoiceItems(invoiceItems);
		var savedInvoice = invoiceRepository.save(invoice);
		return new CheckoutResult(true, "Checkout successful.", savedInvoice.getId());
	}

	@Transactional(readOnly = true)
	public List<Invoice> getInvoicesByUsername(@NotNull String username) {
		return invoiceRepository.findByUserUsernameOrderByIdDesc(username);
	}

	@Transactional(readOnly = true)
	public List<Invoice> getAllInvoices() {
		return invoiceRepository.findAll().stream()
				.sorted((a, b) -> Long.compare(b.getId(), a.getId()))
				.toList();
	}

	@Transactional(readOnly = true)
	public Optional<Invoice> getInvoiceByIdAndUsername(Long invoiceId, @NotNull String username) {
		return invoiceRepository.findByIdAndUserUsername(invoiceId, username);
	}

	@Transactional(readOnly = true)
	public Optional<Invoice> getInvoiceById(Long invoiceId) {
		return invoiceRepository.findById(invoiceId);
	}

	public void deleteInvoice(Long invoiceId) {
		invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
			invoice.getInvoiceItems().forEach(item -> {
				var book = item.getBook();
				if (book != null) {
					int stock = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
					book.setStockQuantity(stock + item.getQuantity());
					bookRepository.save(book);
				}
			});
			invoiceRepository.delete(invoice);
		});
	}

	public record CheckoutResult(boolean success, String message, Long invoiceId) {
	}
}
