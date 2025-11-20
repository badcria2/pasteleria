document.addEventListener('DOMContentLoaded', function() {

    // Variables de Autenticación y Roles (Inyectadas por Thymeleaf en index.html)
    // Estas DEBEN ser definidas en el HTML antes de cargar main.js
    console.log("main.js DOMContentLoaded:", window.isAuthenticated, window.isClient); // Este debería ejecutarse después

    const isAuthenticated = typeof window.isAuthenticated !== 'undefined' ? window.isAuthenticated : false;
    const isClient = typeof window.isClient !== 'undefined' ? window.isClient : false;

    // ----------------------------------------------------
    // Funciones de Carrito (interactúan con el backend)
    // ----------------------------------------------------

    async function getCartItemsFromBackend() {
        if (!isAuthenticated || !isClient) {
            return [];
        }
        try {
            const response = await fetch('/carrito/api');
            if (response.ok) {
                return await response.json();
            } else if (response.status === 401) {
                // No autenticado, no deberíamos llegar aquí si isAuthenticated es false
                return [];
            } else {
                console.error('Error al obtener el carrito del backend:', response.statusText);
                return [];
            }
        } catch (error) {
            console.error('Error de red al obtener el carrito:', error);
            return [];
        }
    }

    async function updateCartCount() {
        const cartItems = await getCartItemsFromBackend();
        const count = cartItems.reduce((sum, item) => sum + item.cantidad, 0);
        const cartCountElement = document.getElementById('cart-count');
        if (cartCountElement) {
            cartCountElement.textContent = count;
        }
    }

    // Inicializar el contador del carrito al cargar la página
    updateCartCount();

    // ----------------------------------------------------
    // Funciones de Interacción con la UI (Expuestas globalmente)
    // ----------------------------------------------------

    window.agregarDesdeBoton = async function(btn) {
        const productoId = parseInt(btn.dataset.id);
        const nombre = btn.dataset.nombre; // Para el mensaje de alert

        if (isAuthenticated && isClient) {
            try {
                const response = await fetch('/carrito/api/add', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        // Spring Security CSRF token (si está habilitado)
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content // Asegúrate de tener este meta tag
                    },
                    body: `productoId=${productoId}&cantidad=1`
                });

                if (response.ok) {
                    alert(`${nombre} ha sido añadido al carrito.`);
                    updateCartCount(); // Actualizar el contador después de añadir
                } else {
                    const errorText = await response.text();
                    alert(`Error al añadir producto: ${errorText}`);
                }
            } catch (error) {
                console.error('Error de red al añadir producto:', error);
                alert('Error de conexión al añadir producto.');
            }
        } else {
            // Si no está autenticado o no es cliente, redirige al login
            alert('Para agregar productos al carrito, por favor inicia sesión como cliente.');
            window.location.href = '/login';
        }
    };

    // Función para navegación suave
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

}); // Fin de DOMContentLoaded