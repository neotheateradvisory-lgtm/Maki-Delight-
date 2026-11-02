package com.example.makidelight.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.makidelight.service.UsuarioService;

@Controller
public class AuthController {
	private final UsuarioService usuarioService;

	public AuthController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/registro")
	public String registro() {
		return "registro";
	}

	@PostMapping("/registro")
	public String registrar(@RequestParam String name, @RequestParam String email,
			@RequestParam String phone, @RequestParam String password,
			@RequestParam String confirmPassword, Model model) {
		if (!password.equals(confirmPassword)) {
			model.addAttribute("registrationError", "Las contraseñas no coinciden.");
			populateRegistrationForm(model, name, email, phone);
			return "registro";
		}
		try {
			usuarioService.registrar(name, email, phone, password);
			model.addAttribute("registrationSuccess", "Cuenta creada. Ya puedes iniciar sesión.");
			return "login";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("registrationError", exception.getMessage());
			populateRegistrationForm(model, name, email, phone);
			return "registro";
		}
	}

	private void populateRegistrationForm(Model model, String name, String email, String phone) {
		model.addAttribute("registrationName", name);
		model.addAttribute("registrationEmail", email);
		model.addAttribute("registrationPhone", phone);
	}
}
