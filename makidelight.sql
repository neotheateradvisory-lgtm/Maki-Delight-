-- Base de datos para Maki Delight
-- Compatible con MySQL 8.0.16 o superior (incluye soporte para CHECK).
-- Ejecuta este archivo desde MySQL Workbench. No elimina bases ni datos existentes.
--
-- La aplicación utiliza JPA con ddl-auto=validate; ejecuta este archivo antes de iniciarla.

CREATE DATABASE IF NOT EXISTS makidelight
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE makidelight;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre_completo VARCHAR(100) NOT NULL,
    correo VARCHAR(254) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_correo (correo),
    KEY idx_usuarios_rol_activo (rol, activo),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('CLIENTE', 'ADMIN'))
) ENGINE = InnoDB;

ALTER TABLE usuarios
    MODIFY COLUMN rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE';

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categorias_nombre (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS productos (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(12) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    categoria_id BIGINT UNSIGNED NOT NULL,
    imagen_url VARCHAR(2048) NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_productos_codigo (codigo),
    KEY idx_productos_categoria_disponible (categoria_id, disponible),
    CONSTRAINT chk_productos_precio_positivo CHECK (precio > 0),
    CONSTRAINT fk_productos_categoria
        FOREIGN KEY (categoria_id) REFERENCES categorias (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    cliente_nombre VARCHAR(100) NOT NULL,
    cliente_telefono VARCHAR(20) NOT NULL,
    direccion_entrega VARCHAR(300) NOT NULL,
    observaciones TEXT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    subtotal DECIMAL(12, 2) NOT NULL,
    costo_envio DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total DECIMAL(12, 2) NOT NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pedidos_codigo (codigo),
    KEY idx_pedidos_usuario_fecha (usuario_id, creado_en),
    KEY idx_pedidos_estado_fecha (estado, creado_en),
    CONSTRAINT chk_pedidos_subtotal CHECK (subtotal > 0),
    CONSTRAINT chk_pedidos_costo_envio CHECK (costo_envio >= 0),
    CONSTRAINT chk_pedidos_total CHECK (total = subtotal + costo_envio),
    CONSTRAINT chk_pedidos_estado CHECK (estado IN ('PENDIENTE', 'EN_PREPARACION', 'ENTREGADO', 'CANCELADO')),
    CONSTRAINT fk_pedidos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE = InnoDB;

ALTER TABLE pedidos
    MODIFY COLUMN estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE';

CREATE TABLE IF NOT EXISTS pedido_detalles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT UNSIGNED NOT NULL,
    producto_id BIGINT UNSIGNED NULL,
    codigo_producto VARCHAR(12) NOT NULL,
    nombre_producto VARCHAR(120) NOT NULL,
    cantidad SMALLINT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal_linea DECIMAL(12, 2)
        GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    PRIMARY KEY (id),
    KEY idx_detalles_pedido (pedido_id),
    KEY idx_detalles_producto (producto_id),
    CONSTRAINT chk_detalles_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalles_precio CHECK (precio_unitario >= 0),
    CONSTRAINT fk_detalles_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_detalles_producto
        FOREIGN KEY (producto_id) REFERENCES productos (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE = InnoDB;

INSERT IGNORE INTO categorias (nombre, descripcion) VALUES
    ('Clásicos', 'Makis clásicos preparados con ingredientes frescos.'),
    ('Especiales', 'Combinaciones especiales de la casa.'),
    ('Vegetarianos', 'Opciones vegetarianas del menú.');

INSERT IGNORE INTO productos
    (codigo, nombre, descripcion, precio, categoria_id, imagen_url, disponible)
SELECT
    'P001',
    'Maki Clásico',
    'Salmón fresco, palta cremosa y queso crema, envueltos en arroz y nori.',
    20.00,
    categorias.id,
    'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=1000&q=85',
    TRUE
FROM categorias
WHERE categorias.nombre = 'Clásicos';

INSERT IGNORE INTO productos
    (codigo, nombre, descripcion, precio, categoria_id, imagen_url, disponible)
SELECT
    'P002',
    'Maki Especial',
    'Una combinación irresistible de langostino, palta y nuestra salsa especial.',
    25.00,
    categorias.id,
    'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?auto=format&fit=crop&w=1000&q=85',
    TRUE
FROM categorias
WHERE categorias.nombre = 'Especiales';

INSERT IGNORE INTO productos
    (codigo, nombre, descripcion, precio, categoria_id, imagen_url, disponible)
SELECT
    'P003',
    'Maki Acevichado',
    'Maki crocante cubierto con nuestra cremosa salsa acevichada y toques cítricos.',
    28.00,
    categorias.id,
    'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?auto=format&fit=crop&w=1000&q=85',
    TRUE
FROM categorias
WHERE categorias.nombre = 'Especiales';
