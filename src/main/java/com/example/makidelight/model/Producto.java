package com.example.makidelight.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 12, unique = true)
	private String codigo;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String descripcion;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal precio;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria;

	@Column(name = "imagen_url", length = 2048)
	private String imagenUrl;

	@Column(nullable = false)
	private boolean disponible = true;

	@CreationTimestamp
	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@UpdateTimestamp
	@Column(name = "actualizado_en", nullable = false)
	private LocalDateTime actualizadoEn;

	protected Producto() {
	}

	public Producto(String codigo, String nombre, String descripcion, BigDecimal precio,
			Categoria categoria, String imagenUrl, boolean disponible) {
		this.codigo = codigo;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.categoria = categoria;
		this.imagenUrl = imagenUrl;
		this.disponible = disponible;
	}

	public void actualizar(String nombre, String descripcion, BigDecimal precio,
			Categoria categoria, String imagenUrl, boolean disponible) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.categoria = categoria;
		this.imagenUrl = imagenUrl;
		this.disponible = disponible;
	}

	public Long getId() { return id; }
	public String getCodigo() { return codigo; }
	public String getNombre() { return nombre; }
	public String getDescripcion() { return descripcion; }
	public BigDecimal getPrecio() { return precio; }
	public Categoria getCategoria() { return categoria; }
	public String getImagenUrl() { return imagenUrl; }
	public boolean isDisponible() { return disponible; }

	public String getCode() { return codigo; }
	public String getName() { return nombre; }
	public String getDescription() { return descripcion; }
	public BigDecimal getPrice() { return precio; }
	public String getCategory() { return categoria.getNombre(); }
	public String getImage() { return imagenUrl; }
	public boolean getAvailable() { return disponible; }
}
