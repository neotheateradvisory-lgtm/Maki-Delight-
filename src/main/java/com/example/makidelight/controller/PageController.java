package com.example.makidelight.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.makidelight.model.Producto;
import com.example.makidelight.service.PedidoService;
import com.example.makidelight.service.ProductoService;

@Controller
public class PageController {
	private final ProductoService productoService;
	private final PedidoService pedidoService;

	public PageController(ProductoService productoService, PedidoService pedidoService) {
		this.productoService = productoService;
		this.pedidoService = pedidoService;
	}

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("featuredProducts", featuredProducts());
		return "index";
	}

	@GetMapping("/menu")
	public String menu(Model model) {
		model.addAttribute("products", productoService.listarDisponibles());
		return "menu";
	}

	@GetMapping("/carrito")
	public String carrito() {
		return "carrito";
	}

	@GetMapping("/realizar-pedido")
	public String realizarPedido() {
		return "realizar-pedido";
	}

	@GetMapping({ "/producto", "/producto/{codigo}" })
	public String product(@PathVariable(required = false) String codigo, Model model) {
		List<Producto> productos = productoService.listarDisponibles();
		if (productos.isEmpty()) {
			return "redirect:/menu";
		}
		Producto producto = codigo == null
				? productos.getFirst()
				: productos.stream()
						.filter(candidate -> candidate.getCodigo().equalsIgnoreCase(codigo))
						.findFirst()
						.orElse(productos.getFirst());
		model.addAttribute("product", producto);
		model.addAttribute("productCode", producto.getCodigo());
		return "producto";
	}

	@GetMapping("/mis-pedidos")
	public String misPedidos(@AuthenticationPrincipal UserDetails user, Model model) {
		model.addAttribute("orders", user == null ? List.of() : pedidoService.listarDeUsuario(user.getUsername()));
		return "mis-pedidos";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("products", productoService.listarTodos());
		model.addAttribute("categories", productoService.listarCategorias());
		model.addAttribute("orders", pedidoService.listarTodos());
		model.addAttribute("productCount", productoService.contarDisponibles());
		model.addAttribute("orderCount", pedidoService.contarTodos());
		model.addAttribute("pendingCount", pedidoService.contarPendientes());
		return "dashboard";
	}

	private List<Producto> featuredProducts() {
		List<Producto> products = productoService.listarDisponibles();
		return products.subList(0, Math.min(3, products.size()));
	}
}
