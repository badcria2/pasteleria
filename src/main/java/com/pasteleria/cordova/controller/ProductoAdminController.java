package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.model.Producto;
import com.pasteleria.cordova.repository.PedidoRepository;
import com.pasteleria.cordova.service.PedidoService;
import com.pasteleria.cordova.service.ProductoService;
import com.pasteleria.cordova.service.NotificacionService;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class ProductoAdminController {

	@Autowired
	private ProductoService productoService;

	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private NotificacionService notificacionService;



	// Listar productos
	@GetMapping("/productos")
	public String listarProductos(@RequestParam(value = "search", required = false) String search, Model model) {
		List<Producto> productos;
		
		if (search != null && !search.trim().isEmpty()) {
			productos = productoService.searchProductos(search.trim());
			model.addAttribute("search", search);
		} else {
			productos = productoService.findAllProductos();
		}
		
		model.addAttribute("productos", productos);
		
		// Agregar notificaciones para la topbar
		List<Map<String, Object>> notificacionesAlertas = notificacionService.obtenerNotificacionesAlertas();
		List<Map<String, Object>> notificacionesMensajes = notificacionService.obtenerNotificacionesMensajes();
		model.addAttribute("notificacionesAlertas", notificacionesAlertas);
		model.addAttribute("notificacionesMensajes", notificacionesMensajes);
		
		return "admin/productos";
	}

	// Formulario nuevo producto
	@GetMapping("/productos/nuevo")
	public String nuevoProductoForm(Model model) {
		model.addAttribute("producto", new Producto());
		
		// Agregar notificaciones para la topbar
		List<Map<String, Object>> notificacionesAlertas = notificacionService.obtenerNotificacionesAlertas();
		List<Map<String, Object>> notificacionesMensajes = notificacionService.obtenerNotificacionesMensajes();
		model.addAttribute("notificacionesAlertas", notificacionesAlertas);
		model.addAttribute("notificacionesMensajes", notificacionesMensajes);
		
		return "admin/producto-form";
	}

	// Guardar o actualizar producto
	@PostMapping("/productos/save")
	public String guardarProducto(@ModelAttribute Producto producto,
								  @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
								  RedirectAttributes redirectAttributes) {
		// Manejo simple de subida de imagen
		if (imagenFile != null && !imagenFile.isEmpty()) {
			try {
				String uploadDir = "src/main/resources/static/uploads/productos";
				Path uploadPath = Paths.get(uploadDir);
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				String originalFilename = imagenFile.getOriginalFilename();
				// Puedes mejorar el nombre para evitar colisiones
				Path filePath = uploadPath.resolve(originalFilename);
				imagenFile.transferTo(filePath.toFile());
				// Guardar path relativo para servir desde /uploads/productos/
				producto.setImagen("/uploads/productos/" + originalFilename);
			} catch (IOException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "Error al subir la imagen: " + e.getMessage());
				return "redirect:/admin/productos";
			}
		}

		productoService.saveProducto(producto);
		redirectAttributes.addFlashAttribute("successMessage", "Producto guardado correctamente.");
		return "redirect:/admin/productos";
	}

	// Editar producto
	@GetMapping("/productos/editar/{id}")
	public String editarProducto(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
		Producto producto = productoService.findById(id).orElse(null);
		if (producto == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
			return "redirect:/admin/productos";
		}
		model.addAttribute("producto", producto);
		
		// Agregar notificaciones para la topbar
		List<Map<String, Object>> notificacionesAlertas = notificacionService.obtenerNotificacionesAlertas();
		List<Map<String, Object>> notificacionesMensajes = notificacionService.obtenerNotificacionesMensajes();
		model.addAttribute("notificacionesAlertas", notificacionesAlertas);
		model.addAttribute("notificacionesMensajes", notificacionesMensajes);
		
		return "admin/producto-form";
	}

	// Eliminar producto
	@GetMapping("/productos/eliminar/{id}")
	public String eliminarProducto(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		productoService.deleteProducto(id);
		redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado.");
		return "redirect:/admin/productos";
	}

	// También aceptar POST para eliminar (formularios con CSRF)
	@PostMapping("/productos/eliminar/{id}")
	public String eliminarProductoPost(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		productoService.deleteProducto(id);
		redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado.");
		return "redirect:/admin/productos";
	}

	// Listar pedidos (vista administrador)
	@GetMapping("/pedidos")
	public String listarPedidos(@RequestParam(value = "estado", required = false) String estado, Model model) {
		// Obtener pedidos filtrados por estado
		List<Pedido> pedidos = pedidoService.findPedidosByEstado(estado);
		model.addAttribute("pedidos", pedidos);
		model.addAttribute("filtroEstado", estado);
		
		// Agregar estadísticas para las cards del dashboard
		model.addAttribute("totalPendientes", pedidoService.countPedidosByEstado("PENDIENTE"));
		model.addAttribute("totalEnProceso", pedidoService.countPedidosByEstado("EN_PROCESO"));
		model.addAttribute("totalCompletados", pedidoService.countPedidosByEstado("COMPLETADO"));
		model.addAttribute("totalCancelados", pedidoService.countPedidosByEstado("CANCELADO"));
		
		// Agregar notificaciones para la topbar
		List<Map<String, Object>> notificacionesAlertas = notificacionService.obtenerNotificacionesAlertas();
		List<Map<String, Object>> notificacionesMensajes = notificacionService.obtenerNotificacionesMensajes();
		model.addAttribute("notificacionesAlertas", notificacionesAlertas);
		model.addAttribute("notificacionesMensajes", notificacionesMensajes);
		
		return "admin/pedidos";
	}

	// Actualizar estado de un pedido desde la vista admin
	@PostMapping("/pedidos/{id}/estado")
	public String actualizarEstadoPedido(@PathVariable Integer id, 
	                                     @RequestParam(value = "nuevoEstado", required = false) String nuevoEstado,
	                                     @RequestParam(value = "estado", required = false) String estado,
	                                     RedirectAttributes redirectAttributes) {
		
		System.out.println("=== ACTUALIZAR ESTADO PEDIDO ===");
		System.out.println("ID: " + id);
		System.out.println("nuevoEstado param: " + nuevoEstado);
		System.out.println("estado param: " + estado);
		
		// Determinar cuál parámetro se está usando
		String estadoFinal = nuevoEstado != null ? nuevoEstado : estado;
		
		if (estadoFinal == null) {
			System.out.println("ERROR: No se recibió ningún parámetro de estado");
			redirectAttributes.addFlashAttribute("errorMessage", "No se recibió el parámetro de estado");
			return "redirect:/admin/pedidos";
		}
		
		try {
			// Validar estados permitidos
			List<String> estadosPermitidos = List.of("PENDIENTE", "EN_PROCESO", "COMPLETADO", "CANCELADO");
			if (!estadosPermitidos.contains(estadoFinal)) {
				redirectAttributes.addFlashAttribute("errorMessage", "Estado no válido: " + estadoFinal);
				return "redirect:/admin/pedidos";
			}
			
			// Usar el método existente del servicio que ya funciona
			pedidoService.actualizarEstadoPedido(id, estadoFinal);
			redirectAttributes.addFlashAttribute("successMessage", "Estado del pedido #" + id + " actualizado a: " + estadoFinal);
			
		} catch (RuntimeException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el pedido: " + ex.getMessage());
		}
		
		return "redirect:/admin/pedidos";
	}

}
