package com.example.makidelight.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.makidelight.model.Categoria;
import com.example.makidelight.model.Producto;
import com.example.makidelight.repository.CategoriaRepository;
import com.example.makidelight.repository.ProductoRepository;

@Service
public class ProductoService {
	private static final String DEFAULT_IMAGE = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=1000&q=85";
	private final ProductoRepository productoRepository;
	private final CategoriaRepository categoriaRepository;

	public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
		this.productoRepository = productoRepository;
		this.categoriaRepository = categoriaRepository;
	}

	@Transactional(readOnly = true)
	public List<Producto> listarDisponibles() {
		return productoRepository.findAllByDisponibleTrueOrderByIdAsc();
	}

	@Transactional(readOnly = true)
	public List<Producto> listarTodos() {
		return productoRepository.findAllByOrderByIdAsc();
	}

	@Transactional(readOnly = true)
	public Producto buscarDisponible(String codigo) {
		return productoRepository.findByCodigoIgnoreCase(codigo)
				.filter(Producto::isDisponible)
				.orElseThrow(() -> new IllegalArgumentException("El producto no existe o no está disponible."));
	}

	@Transactional(readOnly = true)
	public List<Categoria> listarCategorias() {
		return categoriaRepository.findAllByActivaTrueOrderByNombreAsc();
	}

	@Transactional(readOnly = true)
	public long contarDisponibles() {
		return productoRepository.countByDisponibleTrue();
	}

	@Transactional
	public Producto guardar(String codigoOriginal, String codigo, String nombre, String descripcion,
			BigDecimal precio, String nombreCategoria, String imagenUrl, boolean disponible) {
		codigo = requireText(codigo, "El código del producto es obligatorio.");
		nombre = requireText(nombre, "El nombre del producto es obligatorio.");
		descripcion = requireText(descripcion, "La descripción es obligatoria.");
		nombreCategoria = requireText(nombreCategoria, "Selecciona una categoría.");
		if (!codigo.matches("[A-Za-z0-9]{2,12}")) {
			throw new IllegalArgumentException("El código debe tener entre 2 y 12 letras o números.");
		}
		if (nombre.length() > 120 || descripcion.length() > 65535) {
			throw new IllegalArgumentException("El nombre o la descripción superan el límite permitido.");
		}
		if (precio == null || precio.scale() > 2 || precio.signum() <= 0 || precio.precision() > 10) {
			throw new IllegalArgumentException("El precio debe ser mayor a cero y tener hasta dos decimales.");
		}
		if (imagenUrl != null && imagenUrl.length() > 2048) {
			throw new IllegalArgumentException("La URL de la imagen es demasiado larga.");
		}
		if (imagenUrl != null && !imagenUrl.isBlank()) {
			URI uri;
			try {
				uri = URI.create(imagenUrl.trim());
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Ingresa una URL de imagen válida.", exception);
			}
			String scheme = uri.getScheme();
			if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
				throw new IllegalArgumentException("La URL de la imagen debe usar HTTP o HTTPS.");
			}
		}

		Categoria categoria = categoriaRepository.findByNombreIgnoreCaseAndActivaTrue(nombreCategoria)
				.orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe."));
		if (codigoOriginal == null || codigoOriginal.isBlank()) {
			if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
				throw new IllegalArgumentException("Ya existe un producto con ese código.");
			}
			return productoRepository.save(new Producto(codigo, nombre, descripcion, precio, categoria,
					blankToNull(imagenUrl), disponible));
		}

		Producto producto = productoRepository.findByCodigoIgnoreCase(codigoOriginal)
				.orElseThrow(() -> new IllegalArgumentException("No se encontró el producto que deseas editar."));
		if (!producto.getCodigo().equalsIgnoreCase(codigo)) {
			throw new IllegalArgumentException("El código del producto no se puede cambiar.");
		}
		producto.actualizar(nombre, descripcion, precio, categoria, blankToNull(imagenUrl), disponible);
		return producto;
	}

	@Transactional
	public void eliminar(String codigo) {
		Producto producto = productoRepository.findByCodigoIgnoreCase(codigo)
				.orElseThrow(() -> new IllegalArgumentException("No se encontró el producto."));
		productoRepository.delete(producto);
	}

	private String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? DEFAULT_IMAGE : value.trim();
	}
}
