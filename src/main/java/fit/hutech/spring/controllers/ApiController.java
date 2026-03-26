package fit.hutech.spring.controllers;

import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.CategoryService;
import fit.hutech.spring.viewmodels.BookGetVm;
import fit.hutech.spring.viewmodels.BookPostVm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {
	private final BookService bookService;
	private final CategoryService categoryService;
	private final CartService cartService;

	@GetMapping("/books")
	public ResponseEntity<List<BookGetVm>> getAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
		return ResponseEntity.ok(bookService.getAllBooks(
						pageNo == null ? 0 : pageNo,
						pageSize == null ? 20 : pageSize,
						sortBy == null ? "id" : sortBy)
				.stream()
				.map(BookGetVm::from)
				.toList());
	}

	@GetMapping("/books/{id}")
	public ResponseEntity<BookGetVm> getBookById(@PathVariable Long id) {
		return bookService.getBookById(id)
				.map(book -> ResponseEntity.ok(BookGetVm.from(book)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/books")
	public ResponseEntity<BookGetVm> createBook(@RequestBody BookPostVm vm) {
		var category = categoryService.getCategoryById(vm.categoryId()).orElse(null);
		if (category == null) {
			return ResponseEntity.badRequest().build();
		}
		var book = new fit.hutech.spring.entities.Book();
		book.setTitle(vm.title());
		book.setAuthor(vm.author());
		book.setPrice(vm.price());
		book.setImageUrl(vm.imageUrl());
		book.setDescription(vm.description());
		book.setStockQuantity(vm.stockQuantity());
		book.setCategory(category);
		bookService.addBook(book);
		return ResponseEntity.ok(BookGetVm.from(book));
	}

	@PutMapping("/books/{id}")
	public ResponseEntity<BookGetVm> updateBook(@PathVariable Long id, @RequestBody BookPostVm vm) {
		var existingBook = bookService.getBookById(id).orElse(null);
		if (existingBook == null) {
			return ResponseEntity.notFound().build();
		}
		var category = categoryService.getCategoryById(vm.categoryId()).orElse(null);
		if (category == null) {
			return ResponseEntity.badRequest().build();
		}
		existingBook.setTitle(vm.title());
		existingBook.setAuthor(vm.author());
		existingBook.setPrice(vm.price());
		existingBook.setImageUrl(vm.imageUrl());
		existingBook.setDescription(vm.description());
		existingBook.setStockQuantity(vm.stockQuantity());
		existingBook.setCategory(category);
		bookService.updateBook(existingBook);
		return ResponseEntity.ok(BookGetVm.from(existingBook));
	}

	@DeleteMapping("/books/{id}")
	public ResponseEntity<Void> deleteBookById(@PathVariable Long id) {
		bookService.deleteBookById(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/books/search")
	public ResponseEntity<List<BookGetVm>> searchBooks(String keyword) {
		return ResponseEntity.ok(bookService.searchBook(keyword)
				.stream()
				.map(BookGetVm::from)
				.toList());
	}
}
