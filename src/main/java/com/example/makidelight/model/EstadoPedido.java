package com.example.makidelight.model;

public enum EstadoPedido {
	PENDIENTE,
	EN_PREPARACION,
	ENTREGADO,
	CANCELADO;

	public String getEtiqueta() {
		return switch (this) {
			case PENDIENTE -> "Pendiente";
			case EN_PREPARACION -> "En preparación";
			case ENTREGADO -> "Entregado";
			case CANCELADO -> "Cancelado";
		};
	}

	public String getBadge() {
		return switch (this) {
			case PENDIENTE -> "warning";
			case EN_PREPARACION -> "info";
			case ENTREGADO -> "success";
			case CANCELADO -> "secondary";
		};
	}
}
