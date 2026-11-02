package com.example.makidelight.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.makidelight.model.Usuario;
import com.example.makidelight.repository.UsuarioRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
	private final UsuarioRepository usuarioRepository;

	public DatabaseUserDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("No se encontró la cuenta."));
		return User.withUsername(usuario.getCorreo())
				.password(usuario.getPasswordHash())
				.roles(usuario.getRol().name())
				.disabled(!usuario.isActivo())
				.build();
	}
}
