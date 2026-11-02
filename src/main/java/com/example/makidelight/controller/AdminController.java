package com.example.makidelight.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.makidelight.service.PedidoService;
import com.example.makidelight.service.ProductoService;

@Controller
public class AdminController {
	private final ProductoService productoService;
	private final PedidoService pedidoService;

	public AdminController(ProductoService productoService, PedidoService pedidoService) {
		this.productoService = productoService;
		this.pedidoService = pedidoService;
	}

	@PostMapping("/admin/productos")
	public String guardarProducto(@RequestParam(required = false) String originalCode,
			@RequestParam String code, @RequestParam String name, @RequestParam String description,
			@RequestParam BigDecimal price, @RequestParam String category,
			@RequestParam(required = false) String image,
			@RequestParam(defaultValue = "false") boolean available,
			RedirectAttributes redirectAttributes) {
		try {
			productoService.guardar(originalCode, code, name, description, price, category, image, available);
			redirectAttributes.addFlashAttribute("adminSuccess", "Producto guardado correctamente.");
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
		}
		return "redirect:/dashboard";
	}

	@PostMapping("/admin/productos/{codigo}/eliminar")
	public String eliminarProducto(@PathVariable String codigo, RedirectAttributes redirectAttributes) {
		try {
			productoService.eliminar(codigo);
			redirectAttributes.addFlashAttribute("adminSuccess", "Producto eliminado del catálogo.");
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
		}
		return "redirect:/dashboard";
	}

	@PostMapping("/admin/pedidos/{codigo}/estado")
	public String actualizarEstado(@PathVariable String codigo, @RequestParam String status,
			RedirectAttributes redirectAttributes) {
		try {
			pedidoService.actualizarEstado(codigo, status);
			redirectAttributes.addFlashAttribute("adminSuccess", "Estado del pedido actualizado.");
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
		}
		return "redirect:/dashboard";
	}
}
