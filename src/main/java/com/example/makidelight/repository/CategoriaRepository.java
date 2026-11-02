package com.example.makidelight.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.makidelight.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
	Optional<Categoria> findByNombreIgnoreCaseAndActivaTrue(String nombre);
	List<Categoria> findAllByActivaTrueOrderByNombreAsc();
}
