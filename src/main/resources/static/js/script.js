// Carrito de compras (versión local en el navegador)
let carrito = [];

function agregarAlCarrito(nombre, precio) {
    const item = carrito.find(i => i.nombre === nombre);
    if (item) {
        item.cantidad++;
    } else {
        carrito.push({ nombre, precio, cantidad: 1 });
    }
    actualizarCarrito();
    mostrarNotificacion(`${nombre} agregado al carrito`);
}

function eliminarDelCarrito(nombre) {
    carrito = carrito.filter(i => i.nombre !== nombre);
    actualizarCarrito();
}

function cambiarCantidad(nombre, delta) {
    const item = carrito.find(i => i.nombre === nombre);
    if (item) {
        item.cantidad += delta;
        if (item.cantidad <= 0) {
            eliminarDelCarrito(nombre);
        } else {
            actualizarCarrito();
        }
    }
}

function actualizarCarrito() {
    const count = carrito.reduce((sum, item) => sum + item.cantidad, 0);
    document.getElementById('cart-count').textContent = count;

    const carritoItems = document.getElementById('carrito-items');
    if (carrito.length === 0) {
        carritoItems.innerHTML = '<p style="text-align: center; color: #666; padding: 2rem;">Tu carrito está vacío</p>';
        document.getElementById('total-precio').textContent = '0.00';
    } else {
        let html = '';
        let total = 0;
        carrito.forEach(item => {
            const subtotal = item.precio * item.cantidad;
            total += subtotal;
            html += `
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 1rem; border-bottom: 1px solid #eee;">
                    <div style="flex: 1;">
                        <strong>${item.nombre}</strong><br>
                        <span style="color: #666;">S/ ${item.precio.toFixed(2)}</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <button onclick="cambiarCantidad('${item.nombre}', -1)" style="padding: 0.25rem 0.5rem; background: var(--color-naranja); color: white; border: none; border-radius: 4px; cursor: pointer;">-</button>
                        <span style="min-width: 30px; text-align: center;">${item.cantidad}</span>
                        <button onclick="cambiarCantidad('${item.nombre}', 1)" style="padding: 0.25rem 0.5rem; background: var(--color-naranja); color: white; border: none; border-radius: 4px; cursor: pointer;">+</button>
                    </div>
                    <div style="min-width: 80px; text-align: right; font-weight: bold;">
                        S/ ${subtotal.toFixed(2)}
                    </div>
                </div>
            `;
        });
        carritoItems.innerHTML = html;
        document.getElementById('total-precio').textContent = total.toFixed(2);
    }
}

function verCarrito() {
    document.getElementById('carritoModal').classList.add('active');
}

function cerrarCarrito() {
    document.getElementById('carritoModal').classList.remove('active');
}

function vaciarCarrito() {
    if (confirm('¿Estás seguro de vaciar el carrito?')) {
        carrito = [];
        actualizarCarrito();
    }
}

function finalizarCompra() {
    if (carrito.length === 0) {
        alert('Tu carrito está vacío');
        return;
    }
    const total = carrito.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
    let items = '📋 *Mi Pedido:*%0A%0A';
    carrito.forEach(item => {
        items += `• ${item.cantidad}x ${item.nombre} - S/ ${(item.precio * item.cantidad).toFixed(2)}%0A`;
    });
    items += `%0A💰 *Total: S/ ${total.toFixed(2)}*`;
    const mensaje = `Hola, quisiera hacer el siguiente pedido:%0A%0A${items}`;
    window.open(`https://wa.me/51987654321?text=${mensaje}`, '_blank');
}

function mostrarNotificacion(mensaje) {
    const notif = document.createElement('div');
    notif.textContent = mensaje;
    notif.style.cssText = 'position: fixed; bottom: 100px; right: 20px; background: #25D366; color: white; padding: 1rem 1.5rem; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.3); z-index: 9999; animation: slideIn 0.3s ease-out;';
    document.body.appendChild(notif);
    setTimeout(() => {
        notif.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notif.remove(), 300);
    }, 2000);
}

// Búsqueda de productos (funcionalidad manejada por Thymeleaf en el backend)
// Este JS solo refrescará la página con el parámetro de búsqueda si el botón de búsqueda se usa.
// Si se usa el onkeyup, se filtrará solo visualmente en el frontend. Para un filtrado completo con Thymeleaf
// se debería enviar el formulario con cada cambio o al pulsar enter.
function buscarProductos() {
    const searchText = document.getElementById('searchInput').value;
    // Esto enviará el formulario GET al backend para que el controlador filtre los productos
    window.location.href = `/?search=${encodeURIComponent(searchText)}`;
}

// Funcionalidad para abrir/cerrar modal de Login (ahora gestionado por Thymeleaf en rutas separadas /login y /registro)
// Pero mantengo la función para el modal de carrito.
function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

window.onclick = function(e) {
    const carritoModal = document.getElementById('carritoModal');
    // Para el modal de login/registro, las rutas separadas son preferibles con Spring Security.
    // Este código solo maneja el modal del carrito si se hace clic fuera de él.
    if (e.target === carritoModal) {
        cerrarCarrito();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Inicializar el carrito al cargar la página (si hay algo en local storage o sesión, lo cargaríamos aquí)
    actualizarCarrito();

    // Smooth scroll para anclas
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            if (this.getAttribute('href') !== '#') {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth'
                    });
                }
            }
        });
    });
});