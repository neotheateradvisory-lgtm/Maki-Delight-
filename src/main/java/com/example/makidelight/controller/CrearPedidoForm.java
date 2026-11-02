package com.example.makidelight.controller;

import java.util.ArrayList;
import java.util.List;

public class CrearPedidoForm {
	private String name;
	private String phone;
	private String address;
	private String notes;
	private List<ProductoPedidoForm> items = new ArrayList<>();

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public List<ProductoPedidoForm> getItems() { return items; }
	public void setItems(List<ProductoPedidoForm> items) { this.items = items; }

	public static class ProductoPedidoForm {
		private String code;
		private int quantity;

		public String getCode() { return code; }
		public void setCode(String code) { this.code = code; }
		public int getQuantity() { return quantity; }
		public void setQuantity(int quantity) { this.quantity = quantity; }
	}
}
