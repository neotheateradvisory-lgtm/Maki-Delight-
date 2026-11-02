package com.example.makidelight.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre_completo", nullable = false, length = 100)
	private String nombreCompleto;

	@Column(nullable = false, length = 254, unique = true)
	private String correo;

	@Column(nullable = false, length = 20)
	private String telefono;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RolUsuario rol = RolUsuario.CLIENTE;

	@Column(nullable = false)
	private boolean activo = true;

	@CreationTimestamp
	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@UpdateTimestamp
	@Column(name = "actualizado_en", nullable = false)
	private LocalDateTime actualizadoEn;

	protected Usuario() {
	}

	public Usuario(String nombreCompleto, String correo, String telefono, String passwordHash) {
		this.nombreCompleto = nombreCompleto;
		this.correo = correo;
		this.telefono = telefono;
		this.passwordHash = passwordHash;
	}

	public Long getId() { return id; }
	public String getNombreCompleto() { return nombreCompleto; }
	public String getCorreo() { return correo; }
	public String getTelefono() { return telefono; }
	public String getPasswordHash() { return passwordHash; }
	public RolUsuario getRol() { return rol; }
	public boolean isActivo() { return activo; }
	public LocalDateTime getCreadoEn() { return creadoEn; }
	public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
