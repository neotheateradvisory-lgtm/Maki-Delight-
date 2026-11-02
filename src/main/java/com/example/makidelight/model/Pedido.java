package com.example.makidelight.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 20, unique = true)
	private String codigo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(name = "cliente_nombre", nullable = false, length = 100)
	private String clienteNombre;

	@Column(name = "cliente_telefono", nullable = false, length = 20)
	private String clienteTelefono;

	@Column(name = "direccion_entrega", nullable = false, length = 300)
	private String direccionEntrega;

	@Column(columnDefinition = "TEXT")
	private String observaciones;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private EstadoPedido estado = EstadoPedido.PENDIENTE;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
	private BigDecimal costoEnvio = BigDecimal.ZERO;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PedidoDetalle> detalles = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@UpdateTimestamp
	@Column(name = "actualizado_en", nullable = false)
	private LocalDateTime actualizadoEn;

	protected Pedido() {
	}

	public Pedido(String codigo, Usuario usuario, String clienteNombre, String clienteTelefono,
			String direccionEntrega, String observaciones, BigDecimal subtotal, BigDecimal costoEnvio) {
		this.codigo = codigo;
		this.usuario = usuario;
		this.clienteNombre = clienteNombre;
		this.clienteTelefono = clienteTelefono;
		this.direccionEntrega = direccionEntrega;
		this.observaciones = observaciones;
		this.subtotal = subtotal;
		this.costoEnvio = costoEnvio;
		this.total = subtotal.add(costoEnvio);
	}

	public void agregarDetalle(PedidoDetalle detalle) {
		detalles.add(detalle);
	}

	public void actualizarEstado(EstadoPedido estado) {
		this.estado = estado;
	}

	public Long getId() { return id; }
	public String getCodigo() { return codigo; }
	public Usuario getUsuario() { return usuario; }
	public String getClienteNombre() { return clienteNombre; }
	public String getClienteTelefono() { return clienteTelefono; }
	public String getDireccionEntrega() { return direccionEntrega; }
	public String getObservaciones() { return observaciones; }
	public EstadoPedido getEstado() { return estado; }
	public BigDecimal getSubtotal() { return subtotal; }
	public BigDecimal getCostoEnvio() { return costoEnvio; }
	public BigDecimal getTotal() { return total; }
	public List<PedidoDetalle> getDetalles() { return detalles; }
	public LocalDateTime getCreadoEn() { return creadoEn; }
	public LocalDateTime getActualizadoEn() { return actualizadoEn; }

	public String getCode() { return codigo; }
	public String getCustomer() { return clienteNombre; }
	public String getStatus() { return estado.getEtiqueta(); }
	public String getBadge() { return estado.getBadge(); }
	public String getDate() { return creadoEn == null ? "" : creadoEn.toLocalDate().toString(); }
}
