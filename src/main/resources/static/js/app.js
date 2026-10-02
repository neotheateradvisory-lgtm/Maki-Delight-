(() => {
	"use strict";

	const CART_KEY = "makiDelight.cart";
	const CATALOG_KEY = "makiDelight.catalog";
	const ORDERS_KEY = "makiDelight.orders";
	const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=1000&q=85";
	const money = (amount) => `S/ ${new Intl.NumberFormat("es-PE", {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	}).format(Number(amount) || 0)}`;

	function readJson(key, fallback) {
		const value = localStorage.getItem(key);
		return value === null ? fallback : JSON.parse(value);
	}

	function readCart() {
		const cart = readJson(CART_KEY, []);
		return Array.isArray(cart) ? cart : [];
	}

	function saveCart(cart) {
		localStorage.setItem(CART_KEY, JSON.stringify(cart));
		updateCartCount();
	}

	function updateCartCount() {
		const count = readCart().reduce((total, item) => total + Number(item.quantity || 0), 0);
		document.querySelectorAll("[data-cart-count]").forEach((badge) => {
			badge.textContent = String(count);
			badge.setAttribute("aria-label", `${count} productos en el carrito`);
		});
	}

	function notify(message) {
		const toastElement = document.getElementById("appToast");
		if (!toastElement || !window.bootstrap) return;
		const messageElement = toastElement.querySelector("[data-toast-message]");
		if (messageElement) messageElement.textContent = message;
		window.bootstrap.Toast.getOrCreateInstance(toastElement, { delay: 2600 }).show();
	}

	function addToCart(product, quantity = 1) {
		const catalog = readCatalogState();
		const selectedProduct = catalog.products[product.code] || product;
		if (catalog.deleted.includes(product.code) || selectedProduct.available === false) {
			notify(`${product.name} no está disponible`);
			return;
		}
		const cart = readCart();
		const existing = cart.find((item) => item.code === selectedProduct.code);
		if (existing) {
			existing.quantity += quantity;
		} else {
			cart.push({
				code: selectedProduct.code,
				name: selectedProduct.name,
				price: Number(selectedProduct.price),
				image: selectedProduct.image || DEFAULT_IMAGE,
				quantity
			});
		}
		saveCart(cart);
		notify(`${selectedProduct.name} agregado al carrito`);
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
			const now = new Date();
			const total = cart.reduce((sum, item) => sum + Number(item.price) * Number(item.quantity), 0);
			const orders = readJson(ORDERS_KEY, []);
			const order = {
				code: `PED${String(now.getTime()).slice(-6)}`,
				customer: orderForm.elements.name.value.trim(),
				total: money(total),
				status: "Pendiente",
				date: now.toLocaleDateString("es-PE")
			};
			orders.unshift(order);
			localStorage.setItem(ORDERS_KEY, JSON.stringify(orders));
			saveCart([]);
			renderCart();
			renderCheckout();
			orderForm.reset();
			orderForm.classList.remove("was-validated");
			document.getElementById("order-success")?.classList.remove("d-none");
			const modal = document.getElementById("confirmOrderModal");
			if (modal && window.bootstrap) {
				modal.addEventListener("hidden.bs.modal", () => orderForm.querySelector('[type="submit"]')?.focus(), { once: true });
				if (document.activeElement instanceof HTMLElement) document.activeElement.blur();
				window.bootstrap.Modal.getOrCreateInstance(modal).hide();
			}
			notify("Pedido confirmado en modo demostración");
		});
	}

	function renderLocalOrders() {
		const body = document.getElementById("local-orders");
		if (!body) return;
		const orders = readJson(ORDERS_KEY, []);
		body.replaceChildren(...orders.map((order) => {
			const row = document.createElement("tr");
			const values = [order.code, order.date, order.customer, order.total];
			values.forEach((value, index) => {
				const cell = document.createElement("td");
				cell.textContent = value;
				if (index === 0) cell.className = "order-code";
				if (index === 3) cell.className = "order-amount";
				row.append(cell);
			});
			const statusCell = document.createElement("td");
			const badge = document.createElement("span");
			badge.className = "badge rounded-pill text-bg-warning";
			badge.textContent = order.status;
			statusCell.append(badge);
			row.append(statusCell);
			return row;
		}));
		if (orders.length > 0 && new URLSearchParams(window.location.search).has("pedido")) {
			document.getElementById("new-order-notice")?.classList.remove("d-none");
		}
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
			event.preventDefault();
			event.stopPropagation();
			checkPasswords();
			form.classList.add("was-validated");
			if (!form.checkValidity()) return;
			const success = form.parentElement.querySelector("[data-form-success]");
			if (success) success.classList.remove("d-none");
			notify(form.dataset.form === "register" ? "Datos de registro validados" : "Datos de ingreso validados");
		});
	});

	function readCatalogState() {
		const state = readJson(CATALOG_KEY, { products: {}, deleted: [] });
		if (!state || typeof state !== "object") return { products: {}, deleted: [] };
		return {
			products: state.products && typeof state.products === "object" ? state.products : {},
			deleted: Array.isArray(state.deleted) ? state.deleted : []
		};
	}

	function getPageProducts() {
		const products = new Map();
		document.querySelectorAll("[data-menu-product], [data-product-row]").forEach((row) => {
			products.set(row.dataset.code, {
				code: row.dataset.code,
				name: row.dataset.name,
				description: row.dataset.description,
				price: Number(row.dataset.price),
				category: row.dataset.category,
				image: row.dataset.image || DEFAULT_IMAGE,
				available: true
			});
		});
		const state = readCatalogState();
		Object.values(state.products).forEach((product) => products.set(product.code, product));
		state.deleted.forEach((code) => products.delete(code));
		return [...products.values()];
	}

	function updateDashboardMetrics() {
		const productCount = document.getElementById("adminProductCount");
		const orderCount = document.getElementById("adminOrderCount");
		const pendingCount = document.getElementById("adminPendingCount");
		if (productCount) productCount.textContent = String(getPageProducts().length);
		if (!orderCount && !pendingCount) return;
		const localOrders = readJson(ORDERS_KEY, []);
		if (orderCount) orderCount.textContent = String(3 + localOrders.length);
		if (pendingCount) {
			pendingCount.textContent = String(1 + localOrders.filter((order) => order.status === "Pendiente").length);
		}
	}

	function renderProductDetail() {
		const detail = document.querySelector("[data-product-detail]");
		if (!detail) return;
		const code = detail.dataset.code;
		const state = readCatalogState();
		const product = state.products[code];
		const addButton = detail.querySelector(".detail-add-button");
		if (product) {
			detail.querySelectorAll("[data-detail-name]").forEach((element) => {
				element.textContent = product.name;
			});
			const description = detail.querySelector("[data-detail-description]");
			const category = detail.querySelector("[data-detail-category]");
			const productCode = detail.querySelector("[data-detail-code]");
			const price = detail.querySelector("[data-detail-price]");
			const image = detail.querySelector("#productDetailImage");
			if (description) description.textContent = product.description;
			if (category) category.textContent = product.category;
			if (productCode) productCode.textContent = product.code;
			if (price) price.textContent = money(product.price);
			if (image) {
				image.src = product.image || DEFAULT_IMAGE;
				image.alt = product.name;
			}
			if (addButton) {
				addButton.dataset.code = product.code;
				addButton.dataset.name = product.name;
				addButton.dataset.price = String(product.price);
				addButton.dataset.image = product.image || DEFAULT_IMAGE;
				addButton.disabled = product.available === false;
			}
		} else if (state.deleted.includes(code)) {
			if (addButton) addButton.disabled = true;
			const note = detail.querySelector(".detail-footnote");
			if (note) note.textContent = "Este producto ya no está disponible.";
		}
	}

	function renderMenuProducts() {
		const container = document.getElementById("menu-products");
		if (!container) return;
		container.replaceChildren(...getPageProducts().map((product) => {
			const column = document.createElement("div");
			column.className = "col-sm-6 col-lg-4";
			const article = document.createElement("article");
			article.className = "product-card h-100";
			const link = document.createElement("a");
			link.className = "product-image-link";
			link.href = `/producto/${encodeURIComponent(product.code)}`;
			const image = document.createElement("img");
			image.className = "product-image";
			image.src = product.image || DEFAULT_IMAGE;
			image.alt = product.name;
			image.loading = "lazy";
			const category = document.createElement("span");
			category.className = "product-category-pill";
			category.textContent = product.category;
			link.append(image, category);
			const body = document.createElement("div");
			body.className = "product-card-body";
			const heading = document.createElement("div");
			heading.className = "d-flex justify-content-between align-items-start gap-3";
			const info = document.createElement("div");
			const code = document.createElement("p");
			code.className = "product-category";
			code.textContent = product.code;
			const name = document.createElement("h2");
			name.className = "product-name";
			name.textContent = product.name;
			info.append(code, name);
			const price = document.createElement("span");
			price.className = "product-price";
			price.textContent = money(product.price);
			heading.append(info, price);
			const description = document.createElement("p");
			description.className = "product-description";
			description.textContent = product.description;
			const actions = document.createElement("div");
			actions.className = "d-flex gap-2 align-items-center";
			const detail = document.createElement("a");
			detail.className = "btn btn-outline-deep flex-grow-1";
			detail.href = `/producto/${encodeURIComponent(product.code)}`;
			detail.textContent = "Ver detalle";
			const add = document.createElement("button");
			add.className = "btn btn-deep add-cart-button-compact";
			add.type = "button";
			add.textContent = "+";
			add.setAttribute("aria-label", `Agregar ${product.name} al carrito`);
			add.dataset.code = product.code;
			add.dataset.name = product.name;
			add.dataset.price = String(product.price);
			add.dataset.image = product.image || DEFAULT_IMAGE;
			if (product.available === false) {
				add.disabled = true;
				add.setAttribute("aria-label", `${product.name} no disponible`);
			}
			actions.append(detail, add);
			body.append(heading, description, actions);
			article.append(link, body);
			column.append(article);
			return column;
		}));
	}

	const adminTable = document.getElementById("admin-products");
	const adminForm = document.getElementById("adminProductForm");
	if (adminTable && adminForm) {
		let editingCode = null;
		const field = (id) => document.getElementById(id);
		const state = readCatalogState();

		function saveCatalogState() {
			localStorage.setItem(CATALOG_KEY, JSON.stringify(state));
		}

		function createAdminRow(product) {
			const row = document.createElement("tr");
			row.dataset.productRow = "";
			row.dataset.code = product.code;
			row.dataset.name = product.name;
			row.dataset.description = product.description;
			row.dataset.price = String(product.price);
			row.dataset.category = product.category;
			row.dataset.image = product.image || DEFAULT_IMAGE;
			const code = document.createElement("td");
			code.className = "order-code";
			code.textContent = product.code;
			const nameCell = document.createElement("td");
			const productCell = document.createElement("div");
			productCell.className = "admin-product-cell";
			const image = document.createElement("img");
			image.src = product.image || DEFAULT_IMAGE;
			image.alt = product.name;
			const name = document.createElement("span");
			name.textContent = product.name;
			productCell.append(image, name);
			nameCell.append(productCell);
			const price = document.createElement("td");
			price.textContent = money(product.price);
			const actions = document.createElement("td");
			const buttons = document.createElement("div");
			buttons.className = "d-flex gap-1";
			const edit = document.createElement("button");
			edit.className = "btn btn-sm btn-outline-deep";
			edit.type = "button";
			edit.dataset.adminEdit = "";
			edit.textContent = "Editar";
			const remove = document.createElement("button");
			remove.className = "btn btn-sm btn-outline-danger";
			remove.type = "button";
			remove.dataset.adminDelete = "";
			remove.dataset.code = product.code;
			remove.setAttribute("aria-label", `Eliminar ${product.name}`);
			remove.textContent = "×";
			buttons.append(edit, remove);
			actions.append(buttons);
			row.append(code, nameCell, price, actions);
			edit.dataset.code = product.code;
			edit.dataset.name = product.name;
			edit.dataset.description = product.description;
			edit.dataset.price = String(product.price);
			edit.dataset.category = product.category;
			edit.dataset.image = product.image || DEFAULT_IMAGE;
			return row;
		}

		function renderAdminProducts() {
			adminTable.replaceChildren(...getPageProducts().map(createAdminRow));
			updateDashboardMetrics();
		}

		function openProductForm(product = null) {
			adminForm.classList.remove("d-none");
			adminForm.classList.remove("was-validated");
			editingCode = product ? product.code : null;
			field("adminFormTitle").textContent = product ? "Editar producto" : "Nuevo producto";
			field("productCode").value = product ? product.code : "";
			field("productCode").readOnly = Boolean(product);
			field("productName").value = product ? product.name : "";
			field("productDescription").value = product ? product.description : "";
			field("productPrice").value = product ? String(product.price) : "";
			field("productCategory").value = product ? product.category : "";
			field("productImage").value = product && product.image !== DEFAULT_IMAGE ? product.image : "";
			field("productAvailable").checked = product ? product.available !== false : true;
			adminForm.scrollIntoView({ behavior: "smooth", block: "nearest" });
		}

		function closeProductForm() {
			adminForm.reset();
			adminForm.classList.add("d-none");
			adminForm.classList.remove("was-validated");
			field("productCode").readOnly = false;
			editingCode = null;
		}

		function removeProduct(code) {
			const exists = getPageProducts().some((product) => product.code === code);
			if (!exists) return;
			delete state.products[code];
			if (!state.deleted.includes(code)) state.deleted.push(code);
			saveCatalogState();
			renderAdminProducts();
			closeProductForm();
			notify("Producto eliminado del catálogo de demostración");
		}

		renderAdminProducts();
		field("productCode").addEventListener("input", () => field("productCode").setCustomValidity(""));
		document.querySelector("[data-admin-new]")?.addEventListener("click", () => openProductForm());
		document.querySelector("[data-admin-cancel]")?.addEventListener("click", closeProductForm);
		adminTable.addEventListener("click", (event) => {
			const target = event.target;
			if (!(target instanceof Element)) return;
			const edit = target.closest("[data-admin-edit]");
			if (edit) {
				const product = getPageProducts().find((item) => item.code === edit.dataset.code);
				if (product) openProductForm(product);
				return;
			}
			const remove = target.closest("[data-admin-delete]");
			if (remove) removeProduct(remove.dataset.code);
		});
		adminForm.addEventListener("submit", (event) => {
			event.preventDefault();
			event.stopPropagation();
			adminForm.classList.add("was-validated");
			if (!adminForm.checkValidity()) return;
			const code = field("productCode").value.trim();
			const exists = getPageProducts().some((product) => product.code.toLowerCase() === code.toLowerCase() && product.code !== editingCode);
			if (exists) {
				field("productCode").setCustomValidity("El código ya está en uso.");
				adminForm.classList.add("was-validated");
				field("productCode").reportValidity();
				return;
			}
			field("productCode").setCustomValidity("");
			const product = {
				code,
				name: field("productName").value.trim(),
				description: field("productDescription").value.trim(),
				price: Number(field("productPrice").value),
				category: field("productCategory").value,
				image: field("productImage").value.trim() || DEFAULT_IMAGE,
				available: field("productAvailable").checked
			};
			const wasEditing = Boolean(editingCode);
			state.products[code] = product;
			state.deleted = state.deleted.filter((deletedCode) => deletedCode !== code);
			saveCatalogState();
			renderAdminProducts();
			renderMenuProducts();
			closeProductForm();
			notify(wasEditing ? "Producto actualizado" : "Producto guardado en esta demostración");
		});
	}

	updateCartCount();
	renderCart();
	renderCheckout();
	renderLocalOrders();
	renderMenuProducts();
	renderProductDetail();
})();
