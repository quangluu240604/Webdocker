package fit.hutech.spring.viewmodels;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequestVm(@NotBlank String body, @NotBlank String role) {
}
