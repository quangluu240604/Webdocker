package fit.hutech.spring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {
	@GetMapping("/403")
	public String accessDenied() {
		return "errors/403";
	}

	@GetMapping("/404")
	public String notFound() {
		return "errors/404";
	}

	@GetMapping("/500")
	public String internalServerError() {
		return "errors/500";
	}
}
