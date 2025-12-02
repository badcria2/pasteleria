-- Datos de prueba para H2

-- Insertar categorías
INSERT INTO categoria (id, nombre) VALUES (1, 'Tortas');
INSERT INTO categoria (id, nombre) VALUES (2, 'Pasteles');
INSERT INTO categoria (id, nombre) VALUES (3, 'Postres');

-- Insertar un producto
INSERT INTO producto (id, nombre, precio, descripcion, imagen, categoria_id, stock, disponible) 
VALUES (1, 'Torta de Chocolate', 25.50, 'Deliciosa torta de chocolate', 'chocolate.jpg', 1, 10, true);

-- Insertar un administrador
INSERT INTO administrador (id, nombre, email, usuario, contrasena, estado, fecha_creacion) 
VALUES (1, 'Admin Test', 'admin@test.com', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye6xuKUdHgS.f.h2iK1Mhsmbz.8fTWWpS', true, '2023-01-01 00:00:00');

-- Insertar un cliente (el hash corresponde a "password")
INSERT INTO cliente (cliente_id, nombre, apellido, telefono, direccion, email, password, fecha_registro, estado) 
VALUES (1, 'Cliente', 'Test', '12345678', 'Dirección Test', 'cliente@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6xuKUdHgS.f.h2iK1Mhsmbz.8fTWWpS', '2023-01-01 00:00:00', true);

-- Insertar un carrito
INSERT INTO carrito (id, cliente_id, total, estado, fecha_creacion) 
VALUES (1, 1, 0.0, 'ACTIVO', '2023-01-01 00:00:00');

-- Insertar un pedido
INSERT INTO pedido (id, cliente_id, fecha, estado, total, direccion_envio, nota) 
VALUES (1, 1, '2023-01-01', 'COMPLETADO', 25.50, 'Dirección Test', 'Pedido de prueba');

-- Insertar detalle del pedido
INSERT INTO detalle_pedido (id, pedido_id, producto_id, cantidad, precio, subtotal) 
VALUES (1, 1, 1, 1, 25.50, 25.50);