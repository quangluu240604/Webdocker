package fit.hutech.spring.controllers;

import fit.hutech.spring.services.InvoiceService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
	private final InvoiceService invoiceService;

	@GetMapping
	public String orders(@NotNull Model model, Authentication authentication) {
		if (authentication == null) {
			return "redirect:/login";
		}
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
		var invoices = isAdmin
				? invoiceService.getAllInvoices()
				: invoiceService.getInvoicesByUsername(authentication.getName());
		model.addAttribute("invoices", invoices);
		return "order/list";
	}

	@GetMapping("/{id}")
	public String orderDetail(@PathVariable Long id, Authentication authentication, @NotNull Model model) {
		if (authentication == null) {
			return "redirect:/login";
		}
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
		var invoiceOpt = isAdmin
				? invoiceService.getInvoiceById(id)
				: invoiceService.getInvoiceByIdAndUsername(id, authentication.getName());
		if (invoiceOpt.isEmpty()) {
			return "redirect:/orders";
		}
		model.addAttribute("invoice", invoiceOpt.get());
		return "order/detail";
	}

	@PostMapping("/{id}/delete")
	public String deleteOrder(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
		if (authentication == null) {
			return "redirect:/login";
		}
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
		if (!isAdmin) {
			return "redirect:/orders";
		}
		invoiceService.deleteInvoice(id);
		redirectAttributes.addFlashAttribute("message", "Đơn hàng đã được xóa.");
		return "redirect:/orders";
	}
}
