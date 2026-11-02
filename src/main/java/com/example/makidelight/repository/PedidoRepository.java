package com.example.makidelight.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.makidelight.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
	@EntityGraph(attributePaths = "detalles")
	List<Pedido> findAllByOrderByCreadoEnDesc();

	@EntityGraph(attributePaths = "detalles")
	List<Pedido> findAllByUsuarioCorreoIgnoreCaseOrderByCreadoEnDesc(String correo);

	@EntityGraph(attributePaths = "detalles")
	Optional<Pedido> findByCodigoIgnoreCase(String codigo);

	long countByEstado(com.example.makidelight.model.EstadoPedido estado);
}
