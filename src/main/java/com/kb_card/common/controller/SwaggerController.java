package com.kb_card.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerController {

	@GetMapping("/swagger")
	public String swagger() {
		return "redirect:/swagger-ui/index.html";
	}

}
