package fit.hutech.spring.services;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.repositories.IBookRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class BookService {
	private final IBookRepository bookRepository;

	public List<Book> getAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
		return bookRepository.findAllBooks(pageNo, pageSize, sortBy);
	}

	public List<Book> getAllBooks() {
		return bookRepository.findAll();
	}

	public Optional<Book> getBookById(Long id) {
		return bookRepository.findById(id);
	}

	public void addBook(Book book) {
		bookRepository.save(book);
	}

	public void updateBook(@NotNull Book book) {
		Book existingBook = bookRepository.findById(book.getId()).orElse(null);
		Objects.requireNonNull(existingBook).setTitle(book.getTitle());
		existingBook.setAuthor(book.getAuthor());
		existingBook.setPrice(book.getPrice());
		existingBook.setImageUrl(book.getImageUrl());
		existingBook.setDescription(book.getDescription());
		existingBook.setStockQuantity(book.getStockQuantity());
		existingBook.setCategory(book.getCategory());
		bookRepository.save(existingBook);
	}

	public void deleteBookById(Long id) {
		bookRepository.deleteById(id);
	}
	public List<Book> searchBook(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return bookRepository.findAll();
		}
		String normalizedKeyword = keyword.toLowerCase();
		return bookRepository.findAll().stream()
				.filter(book -> (book.getTitle() != null && book.getTitle().toLowerCase().contains(normalizedKeyword))
						|| (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(normalizedKeyword)))
				.toList();
	}
}
