package fit.hutech.spring.controllers;

import fit.hutech.spring.services.ChatMessageService;
import fit.hutech.spring.viewmodels.ChatMessageRequestVm;
import fit.hutech.spring.viewmodels.ChatMessageVm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/chat-room")
@RequiredArgsConstructor
public class ChatController {
	private final ChatMessageService chatMessageService;

	@GetMapping
	public String chat(Authentication authentication, Model model) {
		boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
				.anyMatch(auth -> "ADMIN".equals(auth.getAuthority()));
		String username = authentication == null ? "Khách" : authentication.getName();
		model.addAttribute("currentUser", username);
		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("messages", chatMessageService.loadConversation());
		return "chat/index";
	}

	@PostMapping(value = "/messages", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity<ChatMessageVm> record(@Valid @RequestBody ChatMessageRequestVm request, Authentication authentication) {
		String username = authentication == null ? "Khách" : authentication.getName();
		String senderLabel = "USER".equals(request.role()) ? username : "Admin GreenShelf";
		var saved = chatMessageService.recordMessage(senderLabel, request.role(), request.body(), username);
		return ResponseEntity.ok(saved);
	}
}

