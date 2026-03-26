package fit.hutech.spring.services;

import fit.hutech.spring.entities.ChatMessage;
import fit.hutech.spring.repositories.ChatMessageRepository;
import fit.hutech.spring.viewmodels.ChatMessageRequestVm;
import fit.hutech.spring.viewmodels.ChatMessageVm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public ChatMessageVm recordMessage(String sender, String role, String body, String username) {
		var chatMessage = ChatMessage.builder()
				.sender(sender)
				.role(role)
				.body(body)
				.username(username)
				.sentAt(LocalDateTime.now())
				.build();
		var saved = chatMessageRepository.save(chatMessage);
		return toVm(saved);
	}

	@Transactional
	public List<ChatMessageVm> loadConversation() {
		return chatMessageRepository.findAllByOrderBySentAtAsc().stream()
				.map(this::toVm)
				.toList();
	}

	private ChatMessageVm toVm(ChatMessage saved) {
		return new ChatMessageVm(
				saved.getSender(),
				saved.getRole(),
				saved.getBody(),
				TIME_FORMATTER.format(saved.getSentAt() != null ? saved.getSentAt() : LocalDateTime.now())
		);
	}
}
