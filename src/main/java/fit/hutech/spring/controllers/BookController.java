package fit.hutech.spring.controllers;

import fit.hutech.spring.daos.Item;
import fit.hutech.spring.entities.Book;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
	private final BookService bookService;
	private final CategoryService categoryService;
	private final CartService cartService;

	@GetMapping
	public String showAllBooks(
			@NotNull Model model,
			@RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "20") Integer pageSize,
			@RequestParam(defaultValue = "id") String sortBy) {
		model.addAttribute("books", bookService.getAllBooks(pageNo, pageSize, sortBy));
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("totalPages", bookService.getAllBooks(pageNo, pageSize, sortBy).size() / pageSize);
		return "book/list";
	}

	@GetMapping("/add")
	public String addBookForm(@NotNull Model model) {
		model.addAttribute("book", new Book());
		model.addAttribute("categories", categoryService.getAllCategories());
		return "book/add";
	}

	@GetMapping("/{id}")
	public String showBookDetail(@PathVariable Long id, @NotNull Model model) {
		var book = bookService.getBookById(id).orElse(null);
		if (book == null) {
			return "redirect:/books";
		}
		model.addAttribute("book", book);
		return "book/detail";
	}

	@PostMapping("/add")
	public String addBook(
			@Valid @ModelAttribute("book") Book book,
			@NotNull BindingResult bindingResult,
			@NotNull Model model,
			@RequestParam(value = "category.id", required = false) Long categoryId) {
		book.setCategory(categoryId != null ? categoryService.getCategoryById(categoryId).orElse(null) : null);
		if (bindingResult.hasErrors()) {
			var errors = bindingResult.getAllErrors()
					.stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage)
					.toArray(String[]::new);
			model.addAttribute("errors", errors);
			model.addAttribute("categories", categoryService.getAllCategories());
			return "book/add";
		}
		bookService.addBook(book);
		return "redirect:/books";
	}

	@GetMapping("/edit/{id}")
	public String editBookForm(Model model, @PathVariable long id) {
		var book = bookService.getBookById(id).orElse(null);
		model.addAttribute("book", book != null ? book : new Book());
		model.addAttribute("categories", categoryService.getAllCategories());
		return "book/edit";
	}

	@GetMapping("/edit")
	public String editBookWithoutId() {
		return "redirect:/books";
	}

	@PostMapping("/edit")
	public String editBook(
			@Valid @ModelAttribute("book") Book book,
			@NotNull BindingResult bindingResult,
			@NotNull Model model,
			@RequestParam(value = "category.id", required = false) Long categoryId) {
		book.setCategory(categoryId != null ? categoryService.getCategoryById(categoryId).orElse(null) : null);
		if (bindingResult.hasErrors()) {
			var errors = bindingResult.getAllErrors()
					.stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage)
					.toArray(String[]::new);
			model.addAttribute("errors", errors);
			model.addAttribute("categories", categoryService.getAllCategories());
			return "book/edit";
		}
		bookService.updateBook(book);
		return "redirect:/books";
	}

	@GetMapping("/delete/{id}")
	public String deleteBook(@PathVariable long id) {
		if (bookService.getBookById(id).isPresent())
			bookService.deleteBookById(id);
		return "redirect:/books";
	}

	@PostMapping("/add-to-cart")
	public String addToCart(HttpSession session,
							@RequestParam long id,
							@RequestParam String name,
							@RequestParam double price,
							@RequestParam(defaultValue = "1") int quantity,
							RedirectAttributes redirectAttributes) {
		var bookOpt = bookService.getBookById(id);
		if (bookOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
			return "redirect:/books";
		}
		var book = bookOpt.get();
		int stock = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
		if (stock <= 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "Book is out of stock.");
			return "redirect:/books";
		}

		var cart = cartService.getCart(session);
		int existingQuantity = cart.getCartItems().stream()
				.filter(item -> item.getBookId().equals(id))
				.mapToInt(Item::getQuantity)
				.findFirst()
				.orElse(0);
		if (existingQuantity + quantity > stock) {
			redirectAttributes.addFlashAttribute("errorMessage", "Cannot add more than available stock.");
			return "redirect:/books";
		}

		cart.addItems(new Item(id, name, book.getPrice(), quantity));
		cartService.updateCart(session, cart);
		redirectAttributes.addFlashAttribute("successMessage", "Added to cart.");
		return "redirect:/books";
	}
}
