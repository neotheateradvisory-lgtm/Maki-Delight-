package com.example.makidelight.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.makidelight.model.EstadoPedido;
import com.example.makidelight.model.Pedido;
import com.example.makidelight.model.PedidoDetalle;
import com.example.makidelight.model.Producto;
import com.example.makidelight.model.Usuario;
import com.example.makidelight.repository.PedidoRepository;
import com.example.makidelight.repository.ProductoRepository;
import com.example.makidelight.repository.UsuarioRepository;

@Service
public class PedidoService {
	private final PedidoRepository pedidoRepository;
	private final ProductoRepository productoRepository;
	private final UsuarioRepository usuarioRepository;

	public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
			UsuarioRepository usuarioRepository) {
		this.pedidoRepository = pedidoRepository;
		this.productoRepository = productoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public Pedido crear(String nombre, String telefono, String direccion, String observaciones,
			String correoUsuario, List<LineaPedido> lineas) {
		nombre = requireText(nombre, "Ingresa el nombre de quien recibirá el pedido.");
		telefono = requireText(telefono, "Ingresa un teléfono.");
		direccion = requireText(direccion, "Ingresa la dirección de entrega.");
		if (nombre.length() > 100 || telefono.length() > 20 || direccion.length() > 300
				|| observaciones != null && observaciones.length() > 65535) {
			throw new IllegalArgumentException("Uno de los datos del pedido supera el límite permitido.");
		}
		if (nombre.length() < 2 || direccion.length() < 5 || !telefono.matches("\\+?[0-9 ]{7,15}")) {
			throw new IllegalArgumentException("Revisa el nombre, teléfono y dirección de entrega.");
		}
		if (lineas == null || lineas.isEmpty() || lineas.size() > 100) {
			throw new IllegalArgumentException("Agrega productos al carrito antes de confirmar.");
		}

		Usuario usuario = correoUsuario == null ? null : usuarioRepository.findByCorreoIgnoreCase(correoUsuario)
				.orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario de la sesión."));
		BigDecimal subtotal = BigDecimal.ZERO;
		List<ProductoCantidad> productos = new ArrayList<>();

		for (LineaPedido linea : lineas) {
			if (linea == null || linea.code() == null || linea.quantity() < 1 || linea.quantity() > 20) {
				throw new IllegalArgumentException("Hay una cantidad o producto inválido en el carrito.");
			}
			Producto producto = productoRepository.findByCodigoIgnoreCase(linea.code())
					.filter(Producto::isDisponible)
					.orElseThrow(() -> new IllegalArgumentException("Uno de los productos ya no está disponible."));
			subtotal = subtotal.add(producto.getPrecio().multiply(BigDecimal.valueOf(linea.quantity())));
			productos.add(new ProductoCantidad(producto, linea.quantity()));
		}

		if (subtotal.signum() <= 0 || subtotal.precision() > 12) {
			throw new IllegalArgumentException("El total del pedido no es válido.");
		}
		Pedido confirmado = new Pedido("PED" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
				.toUpperCase(Locale.ROOT), usuario, nombre, telefono, direccion,
				observaciones, subtotal, BigDecimal.ZERO);
		productos.forEach(linea -> confirmado.agregarDetalle(
				new PedidoDetalle(confirmado, linea.producto(), linea.cantidad())));
		return pedidoRepository.save(confirmado);
	}

	@Transactional(readOnly = true)
	public List<Pedido> listarTodos() {
		return pedidoRepository.findAllByOrderByCreadoEnDesc();
	}

	@Transactional(readOnly = true)
	public List<Pedido> listarDeUsuario(String correo) {
		return pedidoRepository.findAllByUsuarioCorreoIgnoreCaseOrderByCreadoEnDesc(correo);
	}

	@Transactional(readOnly = true)
	public long contarTodos() {
		return pedidoRepository.count();
	}

	@Transactional(readOnly = true)
	public long contarPendientes() {
		return pedidoRepository.countByEstado(EstadoPedido.PENDIENTE);
	}

	@Transactional
	public void actualizarEstado(String codigo, String nuevoEstado) {
		EstadoPedido estado;
		try {
			estado = EstadoPedido.valueOf(nuevoEstado);
		} catch (IllegalArgumentException | NullPointerException exception) {
			throw new IllegalArgumentException("Selecciona un estado válido.");
		}
		Pedido pedido = pedidoRepository.findByCodigoIgnoreCase(codigo)
				.orElseThrow(() -> new IllegalArgumentException("No se encontró el pedido."));
		pedido.actualizarEstado(estado);
	}

	private String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}

	public record LineaPedido(String code, int quantity) {
	}

	private record ProductoCantidad(Producto producto, int cantidad) {
	}
}
