package com.example.makidelight.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.makidelight.service.PedidoService;

@Controller
public class PedidoController {
	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@PostMapping("/realizar-pedido")
	public String crear(@ModelAttribute CrearPedidoForm form, @AuthenticationPrincipal UserDetails user,
			RedirectAttributes redirectAttributes) {
		try {
			if (form.getItems() == null || form.getItems().stream().anyMatch(java.util.Objects::isNull)) {
				throw new IllegalArgumentException("El carrito contiene productos inválidos.");
			}
			List<PedidoService.LineaPedido> lineas = form.getItems().stream()
					.map(item -> new PedidoService.LineaPedido(item.getCode(), item.getQuantity()))
					.toList();
			pedidoService.crear(form.getName(), form.getPhone(), form.getAddress(), form.getNotes(),
					user == null ? null : user.getUsername(), lineas);
			return "redirect:/mis-pedidos?pedido";
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("orderError", exception.getMessage());
			return "redirect:/realizar-pedido";
		}
	}
}
