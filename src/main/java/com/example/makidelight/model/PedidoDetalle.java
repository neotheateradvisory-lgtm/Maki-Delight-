package com.example.makidelight.model;

import java.math.BigDecimal;

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
@Table(name = "pedido_detalles")
public class PedidoDetalle {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_id", nullable = false)
	private Pedido pedido;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "producto_id")
	private Producto producto;

	@Column(name = "codigo_producto", nullable = false, length = 12)
	private String codigoProducto;

	@Column(name = "nombre_producto", nullable = false, length = 120)
	private String nombreProducto;

	@Column(nullable = false)
	private short cantidad;

	@Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
	private BigDecimal precioUnitario;

	@Column(name = "subtotal_linea", insertable = false, updatable = false, precision = 12, scale = 2)
	private BigDecimal subtotalLinea;

	protected PedidoDetalle() {
	}

	public PedidoDetalle(Pedido pedido, Producto producto, int cantidad) {
		this.pedido = pedido;
		this.producto = producto;
		this.codigoProducto = producto.getCodigo();
		this.nombreProducto = producto.getNombre();
		this.cantidad = (short) cantidad;
		this.precioUnitario = producto.getPrecio();
	}

	public Long getId() { return id; }
	public Pedido getPedido() { return pedido; }
	public Producto getProducto() { return producto; }
	public String getCodigoProducto() { return codigoProducto; }
	public String getNombreProducto() { return nombreProducto; }
	public short getCantidad() { return cantidad; }
	public BigDecimal getPrecioUnitario() { return precioUnitario; }
	public BigDecimal getSubtotalLinea() { return subtotalLinea; }
}
