package com.example.makidelight.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80, unique = true)
	private String nombre;

	@Column(length = 255)
	private String descripcion;

	@Column(nullable = false)
	private boolean activa = true;

	@CreationTimestamp
	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@UpdateTimestamp
	@Column(name = "actualizado_en", nullable = false)
	private LocalDateTime actualizadoEn;

	protected Categoria() {
	}

	public Categoria(String nombre, String descripcion) {
		this.nombre = nombre;
		this.descripcion = descripcion;
	}

	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getDescripcion() { return descripcion; }
	public boolean isActiva() { return activa; }
}
