package com.example.makidelight.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.makidelight.model.Usuario;
import com.example.makidelight.repository.UsuarioRepository;

@Service
public class UsuarioService {
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public Usuario registrar(String nombre, String correo, String telefono, String password) {
		String nombreNormalizado = requireText(nombre, "El nombre es obligatorio.");
		String correoNormalizado = requireText(correo, "El correo es obligatorio.").toLowerCase(Locale.ROOT);
		String telefonoNormalizado = requireText(telefono, "El teléfono es obligatorio.");
		if (!correoNormalizado.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
			throw new IllegalArgumentException("Ingresa un correo electrónico válido.");
		}
		if (nombreNormalizado.length() < 2 || nombreNormalizado.length() > 100 || correoNormalizado.length() > 254
				|| telefonoNormalizado.length() > 20) {
			throw new IllegalArgumentException("Uno de los datos ingresados supera el límite permitido.");
		}
		if (!telefonoNormalizado.matches("\\+?[0-9 ]{7,15}")) {
			throw new IllegalArgumentException("Ingresa un teléfono válido.");
		}
		if (password == null || password.length() < 8) {
			throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
		}
		if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new IllegalArgumentException("La contraseña no puede superar los 72 bytes.");
		}
		if (usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
			throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico.");
		}
		return usuarioRepository.save(new Usuario(nombreNormalizado, correoNormalizado,
				telefonoNormalizado, passwordEncoder.encode(password)));
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorCorreo(String correo) {
		return usuarioRepository.findByCorreoIgnoreCase(correo)
				.orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario."));
	}

	private String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}
}
