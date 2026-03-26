package fit.hutech.spring.controllers;

import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.InvoiceService;
import fit.hutech.spring.services.UserService;
import fit.hutech.spring.viewmodels.CheckoutVm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;
	private final BookService bookService;
	private final UserService userService;
	private final InvoiceService invoiceService;

	@GetMapping
	public String showCart(HttpSession session, @NotNull Model model) {
		model.addAttribute("cart", cartService.getCart(session));
		model.addAttribute("totalPrice", cartService.getSumPrice(session));
		model.addAttribute("totalQuantity", cartService.getSumQuantity(session));
		if (!model.containsAttribute("checkoutVm")) {
			model.addAttribute("checkoutVm", new CheckoutVm());
		}
		return "book/cart";
	}

	@GetMapping("/removeFromCart/{id}")
	public String removeFromCart(HttpSession session, @PathVariable Long id) {
		var cart = cartService.getCart(session);
		cart.removeItems(id);
		cartService.updateCart(session, cart);
		return "redirect:/cart";
	}

	@GetMapping("/updateCart/{id}/{quantity}")
	public String updateCart(HttpSession session, @PathVariable Long id, @PathVariable int quantity, RedirectAttributes redirectAttributes) {
		var cart = cartService.getCart(session);
		if (quantity < 1) {
			quantity = 1;
		}

		var bookOpt = bookService.getBookById(id);
		if (bookOpt.isEmpty()) {
			cart.removeItems(id);
			cartService.updateCart(session, cart);
			redirectAttributes.addFlashAttribute("errorMessage", "Book not found. Item removed from cart.");
			return "redirect:/cart";
		}

		int stock = bookOpt.get().getStockQuantity() == null ? 0 : bookOpt.get().getStockQuantity();
		if (stock <= 0) {
			cart.removeItems(id);
			cartService.updateCart(session, cart);
			redirectAttributes.addFlashAttribute("errorMessage", "This book is out of stock and was removed from cart.");
			return "redirect:/cart";
		}
		if (quantity > stock) {
			quantity = stock;
			redirectAttributes.addFlashAttribute("errorMessage", "Quantity adjusted to available stock: " + stock);
		}

		cart.updateItems(id, quantity);
		cartService.updateCart(session, cart);
		return "redirect:/cart";
	}

	@GetMapping("/clearCart")
	public String clearCart(HttpSession session) {
		cartService.removeCart(session);
		return "redirect:/cart";
	}

	@PostMapping("/checkout")
	public String checkout(HttpSession session,
						   Authentication authentication,
						   @Valid @ModelAttribute("checkoutVm") CheckoutVm checkoutVm,
						   BindingResult bindingResult,
						   @NotNull Model model,
						   RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("cart", cartService.getCart(session));
			model.addAttribute("totalPrice", cartService.getSumPrice(session));
			model.addAttribute("totalQuantity", cartService.getSumQuantity(session));
			return "book/cart";
		}

		if (authentication == null || authentication.getName() == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Please login to checkout.");
			return "redirect:/login";
		}

		var userOpt = userService.findByUsername(authentication.getName());
		if (userOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
			return "redirect:/cart";
		}

		var result = invoiceService.checkout(userOpt.get(), cartService.getCart(session), checkoutVm);
		if (!result.success()) {
			redirectAttributes.addFlashAttribute("errorMessage", result.message());
			return "redirect:/cart";
		}

		cartService.removeCart(session);
		redirectAttributes.addFlashAttribute("successMessage", "Checkout successful. Invoice ID: " + result.invoiceId());
		return "redirect:/orders/" + result.invoiceId();
	}
}
