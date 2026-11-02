package com.example.makidelight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import com.example.makidelight.model.Categoria;
import com.example.makidelight.model.Producto;
import com.example.makidelight.repository.CategoriaRepository;
import com.example.makidelight.repository.PedidoRepository;
import com.example.makidelight.repository.ProductoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MakidelightApplicationTests {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private CategoriaRepository categoriaRepository;
	@Autowired
	private ProductoRepository productoRepository;
	@Autowired
	private PedidoRepository pedidoRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void rendersPagesWithPersistentServices() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk());
		mockMvc.perform(get("/menu")).andExpect(status().isOk());
		mockMvc.perform(get("/login")).andExpect(status().isOk());
		mockMvc.perform(get("/registro"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"_csrf\"")));
		mockMvc.perform(get("/carrito")).andExpect(status().isOk());
		mockMvc.perform(get("/realizar-pedido")).andExpect(status().isOk());
		mockMvc.perform(get("/mis-pedidos")).andExpect(status().isOk());
	}

	@Test
	void registrationAndLoginUseStoredAccounts() throws Exception {
		String email = "test-" + System.nanoTime() + "@example.com";
		mockMvc.perform(post("/registro")
						.with(csrf())
						.param("name", "Cliente de prueba")
						.param("email", email)
						.param("phone", "987654321")
						.param("password", "password-segura")
						.param("confirmPassword", "password-segura"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Cuenta creada")));

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", email)
						.param("password", "password-segura"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/menu"));
	}

	@Test
	void dashboardIsRestrictedToAdministrators() throws Exception {
		mockMvc.perform(get("/dashboard").with(user("cliente@example.com").roles("CLIENTE")))
				.andExpect(status().isForbidden());
	}

	@Test
	void checkoutSavesOrdersUsingDatabasePrices() throws Exception {
		String suffix = Long.toString(System.nanoTime());
		Categoria category = categoriaRepository.saveAndFlush(new Categoria("Prueba " + suffix, "Categoría de prueba"));
		productoRepository.saveAndFlush(new Producto("T" + suffix.substring(suffix.length() - 10),
				"Producto de prueba", "Descripción", new BigDecimal("25.50"), category, null, true));

		String code = productoRepository.findAllByOrderByIdAsc().getLast().getCodigo();
		mockMvc.perform(get("/producto/{code}", code)).andExpect(status().isOk());
		mockMvc.perform(get("/menu")).andExpect(status().isOk());
		mockMvc.perform(get("/")).andExpect(status().isOk());
		mockMvc.perform(get("/dashboard").with(user("admin@example.com").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"category\"")))
				.andExpect(content().string(containsString("Prueba " + suffix)));
		mockMvc.perform(post("/realizar-pedido")
						.with(csrf())
						.param("name", "Cliente de prueba")
						.param("phone", "987654321")
						.param("address", "Calle de prueba 123")
						.param("items[0].code", code)
						.param("items[0].quantity", "2"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/mis-pedidos?pedido"));

		assertThat(pedidoRepository.count()).isEqualTo(1);
		assertThat(pedidoRepository.findAllByOrderByCreadoEnDesc().getFirst().getTotal())
				.isEqualByComparingTo("51.00");

		mockMvc.perform(post("/admin/productos")
						.with(user("admin@example.com").roles("ADMIN"))
						.with(csrf())
						.param("originalCode", code)
						.param("code", code)
						.param("name", "Producto actualizado")
						.param("description", "Descripción actualizada")
						.param("price", "30.50")
						.param("category", category.getNombre())
						.param("available", "true"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/dashboard"));
		assertThat(productoRepository.findByCodigoIgnoreCase(code).orElseThrow().getPrecio())
				.isEqualByComparingTo("30.50");

		String newCode = "N" + suffix.substring(suffix.length() - 10);
		mockMvc.perform(post("/admin/productos")
						.with(user("admin@example.com").roles("ADMIN"))
						.with(csrf())
						.param("code", newCode)
						.param("name", "Producto nuevo")
						.param("description", "Descripción nueva")
						.param("price", "19.90")
						.param("category", category.getNombre())
						.param("available", "true"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/dashboard"));
		assertThat(productoRepository.findByCodigoIgnoreCase(newCode)).isPresent();

		mockMvc.perform(post("/admin/productos/{codigo}/eliminar", newCode)
						.with(user("admin@example.com").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/dashboard"));
		assertThat(productoRepository.findByCodigoIgnoreCase(newCode)).isEmpty();
	}
}
