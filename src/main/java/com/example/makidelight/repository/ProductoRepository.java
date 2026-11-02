package com.example.makidelight.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.makidelight.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
	@EntityGraph(attributePaths = "categoria")
	List<Producto> findAllByDisponibleTrueOrderByIdAsc();

	@EntityGraph(attributePaths = "categoria")
	List<Producto> findAllByOrderByIdAsc();

	@EntityGraph(attributePaths = "categoria")
	Optional<Producto> findByCodigoIgnoreCase(String codigo);

	boolean existsByCodigoIgnoreCase(String codigo);
	long countByDisponibleTrue();
}
