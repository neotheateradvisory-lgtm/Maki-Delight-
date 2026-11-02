(() => {
	"use strict";

	const CART_KEY = "makiDelight.cart";
	const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=1000&q=85";
	const money = (amount) => `S/ ${new Intl.NumberFormat("es-PE", {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	}).format(Number(amount) || 0)}`;

	function readCart() {
		const value = localStorage.getItem(CART_KEY);
		if (value === null) return [];
		const cart = JSON.parse(value);
		return Array.isArray(cart) ? cart : [];
	}

	function updateCartCount() {
		const count = readCart().reduce((total, item) => total + Number(item.quantity || 0), 0);
		document.querySelectorAll("[data-cart-count]").forEach((badge) => {
			badge.textContent = String(count);
			badge.setAttribute("aria-label", `${count} productos en el carrito`);
		});
	}

	function saveCart(cart) {
		localStorage.setItem(CART_KEY, JSON.stringify(cart));
		updateCartCount();
	}

	function notify(message) {
		const toastElement = document.getElementById("appToast");
		if (!toastElement || !window.bootstrap) return;
		const messageElement = toastElement.querySelector("[data-toast-message]");
		if (messageElement) messageElement.textContent = message;
		window.bootstrap.Toast.getOrCreateInstance(toastElement, { delay: 2600 }).show();
	}

	function addToCart(product, quantity = 1) {
		const cart = readCart();
		const existing = cart.find((item) => item.code === product.code);
		if (existing) {
			existing.quantity = Math.min(20, existing.quantity + quantity);
		} else {
			cart.push({
				code: product.code,
				name: product.name,
				price: Number(product.price),
				image: product.image || DEFAULT_IMAGE,
				quantity: Math.min(20, quantity)
			});
		}
		saveCart(cart);
		notify(`${product.name} agregado al carrito`);
	}

	function productFromButton(button) {
		return {
			code: button.dataset.code,
			name: button.dataset.name,
			price: button.dataset.price,
			image: button.dataset.image
		};
	}

	document.addEventListener("click", (event) => {
		const target = event.target;
		if (!(target instanceof Element)) return;
		const button = target.closest("[data-add-cart], .add-cart-button, .add-cart-button-compact, .detail-add-button");
		if (!button || button instanceof HTMLButtonElement && button.disabled) return;
		const quantityInput = document.getElementById("productQuantity");
		const requested = quantityInput ? Number(quantityInput.value) : 1;
		const quantity = Number.isInteger(requested) && requested > 0 ? Math.min(requested, 20) : 1;
		addToCart(productFromButton(button), quantity);
	});

	document.querySelectorAll("[data-quantity-change]").forEach((button) => {
		button.addEventListener("click", () => {
			const input = document.getElementById("productQuantity");
			if (!input) return;
			const next = Number(input.value) + Number(button.dataset.quantityChange);
			input.value = String(Math.max(Number(input.min) || 1, Math.min(Number(input.max) || 20, next)));
		});
	});

	function createCartRow(item) {
		const row = document.createElement("tr");
		const productCell = document.createElement("td");
		const product = document.createElement("div");
		product.className = "cart-product";
		const image = document.createElement("img");
		image.src = item.image || DEFAULT_IMAGE;
		image.alt = item.name;
		const productText = document.createElement("div");
		const name = document.createElement("strong");
		name.textContent = item.name;
		const code = document.createElement("span");
		code.textContent = item.code;
		productText.append(name, code);
		product.append(image, productText);
		productCell.append(product);

		const priceCell = document.createElement("td");
		priceCell.textContent = money(item.price);

		const quantityCell = document.createElement("td");
		const quantity = document.createElement("div");
		quantity.className = "cart-quantity";
		const decrease = document.createElement("button");
		decrease.type = "button";
		decrease.dataset.cartChange = "-1";
		decrease.dataset.code = item.code;
		decrease.setAttribute("aria-label", `Reducir cantidad de ${item.name}`);
		decrease.textContent = "−";
		const value = document.createElement("input");
		value.type = "number";
		value.min = "1";
		value.max = "20";
		value.value = String(item.quantity);
		value.dataset.cartQuantity = item.code;
		value.setAttribute("aria-label", `Cantidad de ${item.name}`);
		const increase = document.createElement("button");
		increase.type = "button";
		increase.dataset.cartChange = "1";
		increase.dataset.code = item.code;
		increase.setAttribute("aria-label", `Aumentar cantidad de ${item.name}`);
		increase.textContent = "+";
		quantity.append(decrease, value, increase);
		quantityCell.append(quantity);

		const subtotalCell = document.createElement("td");
		subtotalCell.className = "order-amount";
		subtotalCell.textContent = money(item.price * item.quantity);

		const actionCell = document.createElement("td");
		const remove = document.createElement("button");
		remove.type = "button";
		remove.className = "cart-remove";
		remove.dataset.cartRemove = item.code;
		remove.setAttribute("aria-label", `Eliminar ${item.name} del carrito`);
		remove.textContent = "×";
		actionCell.append(remove);
		row.append(productCell, priceCell, quantityCell, subtotalCell, actionCell);
		return row;
	}

	function renderCart() {
		const container = document.getElementById("cart-items");
		if (!container) return;
		const cart = readCart();
		container.replaceChildren(...cart.map(createCartRow));
		const total = cart.reduce((sum, item) => sum + Number(item.price) * Number(item.quantity), 0);
		const empty = document.getElementById("cart-empty");
		if (empty) empty.classList.toggle("d-none", cart.length > 0);
		const table = container.closest("table");
		if (table) table.classList.toggle("d-none", cart.length === 0);
		const checkoutLink = document.getElementById("checkout-link");
		if (checkoutLink) {
			checkoutLink.classList.toggle("disabled", cart.length === 0);
			checkoutLink.setAttribute("aria-disabled", String(cart.length === 0));
			checkoutLink.tabIndex = cart.length === 0 ? -1 : 0;
		}
		const subtotal = document.getElementById("cart-subtotal");
		const totalElement = document.getElementById("cart-total");
		if (subtotal) subtotal.textContent = money(total);
		if (totalElement) totalElement.textContent = money(total);
	}

	document.addEventListener("click", (event) => {
		const target = event.target;
		if (!(target instanceof Element)) return;
		const removeButton = target.closest("[data-cart-remove]");
		if (removeButton) {
			saveCart(readCart().filter((item) => item.code !== removeButton.dataset.cartRemove));
			renderCart();
			return;
		}
		const changeButton = target.closest("[data-cart-change]");
		if (changeButton) {
			const code = changeButton.dataset.code;
			const difference = Number(changeButton.dataset.cartChange);
			const cart = readCart().map((item) => item.code === code
				? { ...item, quantity: Math.max(1, Math.min(20, Number(item.quantity) + difference)) }
				: item);
			saveCart(cart);
			renderCart();
		}
	});

	document.addEventListener("change", (event) => {
		const target = event.target;
		if (!(target instanceof HTMLInputElement) || !target.matches("[data-cart-quantity]")) return;
		const code = target.dataset.cartQuantity;
		const value = Number(target.value);
		const quantity = Number.isInteger(value) ? Math.max(1, Math.min(20, value)) : 1;
		saveCart(readCart().map((item) => item.code === code ? { ...item, quantity } : item));
		renderCart();
	});

	function renderCheckout() {
		const itemsElement = document.getElementById("checkout-items");
		if (!itemsElement) return;
		const cart = readCart();
		const emptyElement = document.getElementById("checkout-empty");
		if (emptyElement) emptyElement.classList.toggle("d-none", cart.length > 0);
		itemsElement.replaceChildren(...cart.map((item) => {
			const row = document.createElement("div");
			row.className = "checkout-item";
			const product = document.createElement("div");
			const name = document.createElement("strong");
			name.textContent = item.name;
			const quantity = document.createElement("span");
			quantity.textContent = `${item.quantity} ${item.quantity === 1 ? "unidad" : "unidades"}`;
			product.append(name, quantity);
			const subtotal = document.createElement("strong");
			subtotal.textContent = money(item.price * item.quantity);
			row.append(product, subtotal);
			return row;
		}));
		const total = cart.reduce((sum, item) => sum + Number(item.price) * Number(item.quantity), 0);
		const totalElement = document.getElementById("checkout-total");
		if (totalElement) totalElement.textContent = money(total);
	}

	const orderForm = document.getElementById("orderForm");
	if (orderForm) {
		orderForm.addEventListener("submit", (event) => {
			event.preventDefault();
			event.stopPropagation();
			orderForm.classList.add("was-validated");
			if (!orderForm.checkValidity()) return;
			if (readCart().length === 0) {
				notify("Agrega al menos un producto antes de confirmar");
				return;
			}
			const modal = document.getElementById("confirmOrderModal");
			if (modal && window.bootstrap) window.bootstrap.Modal.getOrCreateInstance(modal).show();
		});
	}

	const confirmOrderButton = document.getElementById("confirmOrderButton");
	if (confirmOrderButton && orderForm) {
		confirmOrderButton.addEventListener("click", () => {
			const cart = readCart();
			if (cart.length === 0) return;
			const container = document.getElementById("orderItemsContainer");
			container.replaceChildren(...cart.flatMap((item, index) => {
				const code = document.createElement("input");
				code.type = "hidden";
				code.name = `items[${index}].code`;
				code.value = item.code;
				const quantity = document.createElement("input");
				quantity.type = "hidden";
				quantity.name = `items[${index}].quantity`;
				quantity.value = String(item.quantity);
				return [code, quantity];
			}));
			confirmOrderButton.disabled = true;
			orderForm.submit();
		});
	}

	document.querySelectorAll(".demo-form").forEach((form) => {
		const password = form.querySelector("#registerPassword");
		const confirmation = form.querySelector("#confirmPassword");
		const checkPasswords = () => {
			if (password && confirmation) {
				confirmation.setCustomValidity(password.value === confirmation.value ? "" : "Las contraseñas no coinciden.");
			}
		};
		password?.addEventListener("input", checkPasswords);
		confirmation?.addEventListener("input", checkPasswords);
		form.addEventListener("submit", (event) => {
			checkPasswords();
			form.classList.add("was-validated");
			if (!form.checkValidity()) {
				event.preventDefault();
				event.stopPropagation();
			}
		});
	});

	const adminForm = document.getElementById("adminProductForm");
	if (adminForm) {
		const field = (id) => document.getElementById(id);
		const originalCode = field("originalCode");
		document.querySelector("[data-admin-new]")?.addEventListener("click", () => {
			adminForm.reset();
			adminForm.classList.remove("d-none", "was-validated");
			field("adminFormTitle").textContent = "Nuevo producto";
			field("productCode").readOnly = false;
			originalCode.value = "";
			adminForm.scrollIntoView({ behavior: "smooth", block: "nearest" });
		});

		document.querySelector("[data-admin-cancel]")?.addEventListener("click", () => {
			adminForm.reset();
			adminForm.classList.add("d-none");
			adminForm.classList.remove("was-validated");
			field("productCode").readOnly = false;
			originalCode.value = "";
		});

		document.getElementById("admin-products")?.addEventListener("click", (event) => {
			const target = event.target;
			if (!(target instanceof Element)) return;
			const edit = target.closest("[data-admin-edit]");
			if (!edit) return;
			adminForm.classList.remove("d-none", "was-validated");
			field("adminFormTitle").textContent = "Editar producto";
			field("productCode").value = edit.dataset.code;
			field("productCode").readOnly = true;
			originalCode.value = edit.dataset.code;
			field("productName").value = edit.dataset.name;
			field("productDescription").value = edit.dataset.description;
			field("productPrice").value = edit.dataset.price;
			field("productCategory").value = edit.dataset.category;
			field("productImage").value = edit.dataset.image || "";
			field("productAvailable").checked = edit.dataset.available === "true";
			adminForm.scrollIntoView({ behavior: "smooth", block: "nearest" });
		});
	}

	if (new URLSearchParams(window.location.search).has("pedido")
			&& window.location.pathname === "/mis-pedidos") {
		saveCart([]);
	}

	updateCartCount();
	renderCart();
	renderCheckout();
})();
