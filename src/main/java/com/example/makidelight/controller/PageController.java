package com.example.makidelight.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

	private static final List<Product> PRODUCTS = List.of(
			new Product("P001", "Maki Clásico", "Salmón fresco, palta cremosa y queso crema, envueltos en arroz y nori.",
					20.00, "Clásicos", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=1000&q=85"),
			new Product("P002", "Maki Especial", "Una combinación irresistible de langostino, palta y nuestra salsa especial.",
					25.00, "Especiales", "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?auto=format&fit=crop&w=1000&q=85"),
			new Product("P003", "Maki Acevichado", "Maki crocante cubierto con nuestra cremosa salsa acevichada y toques cítricos.",
					28.00, "Especiales", "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?auto=format&fit=crop&w=1000&q=85"));

	private static final List<Map<String, String>> ORDERS = List.of(
			Map.of("code", "PED001", "customer", "Cliente 1", "total", "S/ 55.00", "status", "Pendiente", "badge", "warning", "date", "01/10/2026"),
			Map.of("code", "PED002", "customer", "Cliente 2", "total", "S/ 40.00", "status", "En preparación", "badge", "info", "date", "30/09/2026"),
			Map.of("code", "PED003", "customer", "Cliente 3", "total", "S/ 65.00", "status", "Entregado", "badge", "success", "date", "29/09/2026"));

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("featuredProducts", PRODUCTS);
		return "index";
	}

	@GetMapping("/menu")
	public String menu(Model model) {
		model.addAttribute("products", PRODUCTS);
		return "menu";
	}

	@GetMapping({ "/producto", "/producto/{codigo}" })
	public String product(@PathVariable(required = false) String codigo, Model model) {
		Product product = PRODUCTS.stream()
				.filter(candidate -> codigo == null || candidate.getCode().equalsIgnoreCase(codigo))
				.findFirst()
				.orElse(PRODUCTS.getFirst());
		model.addAttribute("product", product);
		model.addAttribute("productCode", codigo == null ? product.getCode() : codigo);
		return "producto";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/registro")
	public String registro() {
		return "registro";
	}

	@GetMapping("/carrito")
	public String carrito() {
		return "carrito";
	}

	@GetMapping("/realizar-pedido")
	public String realizarPedido() {
		return "realizar-pedido";
	}

	@GetMapping("/mis-pedidos")
	public String misPedidos(Model model) {
		model.addAttribute("orders", ORDERS);
		return "mis-pedidos";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("products", PRODUCTS);
		model.addAttribute("orders", ORDERS);
		model.addAttribute("productCount", PRODUCTS.size());
		model.addAttribute("orderCount", ORDERS.size());
		model.addAttribute("pendingCount", ORDERS.stream()
				.filter(order -> "Pendiente".equals(order.get("status"))).count());
		return "dashboard";
	}

	public static class Product {
		private final String code;
		private final String name;
		private final String description;
		private final double price;
		private final String category;
		private final String image;

		public Product(String code, String name, String description, double price, String category, String image) {
			this.code = code;
			this.name = name;
			this.description = description;
			this.price = price;
			this.category = category;
			this.image = image;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public double getPrice() {
			return price;
		}

		public String getCategory() {
			return category;
		}

		public String getImage() {
			return image;
		}
	}
}
