package com.pasteleria.cordova.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.pasteleria.cordova.model.DetallePedido;
import com.pasteleria.cordova.model.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PedidoService pedidoService;

    @Transactional(readOnly = true)
    public byte[] generarFacturaPDF(Integer pedidoId) {
        try {
            // Obtener el pedido con todos sus detalles
            Pedido pedido = pedidoService.getPedidoByIdWithDetalles(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

            // Verificar que el pedido esté en estado COMPLETADO para generar factura
            if (!"COMPLETADO".equals(pedido.getEstado())) {
                throw new RuntimeException("No se puede generar factura para pedidos en estado: " + pedido.getEstado());
            }

            // Calcular subtotal
            BigDecimal subtotal = pedido.getDetalles().stream()
                    .map(DetallePedido::getSubTotal)
                    .map(Float::doubleValue)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Crear contexto para Thymeleaf
            Context context = new Context();
            context.setVariable("pedido", pedido);
            context.setVariable("subtotal", subtotal);

            // Procesar template HTML
            String htmlContent = templateEngine.process("invoice/factura-pdf", context);

            // Generar PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Configurar propiedades del conversor
            ConverterProperties properties = new ConverterProperties();
            
            // Convertir HTML a PDF
            HtmlConverter.convertToPdf(htmlContent, outputStream, properties);
            
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando factura PDF: " + e.getMessage(), e);
        }
    }

    public String generarNombreArchivo(Integer pedidoId) {
        return String.format("Factura_Pedido_%d.pdf", pedidoId);
    }

    @Transactional(readOnly = true)
    public boolean puedeGenerarFactura(Integer pedidoId) {
        try {
            Pedido pedido = pedidoService.getPedidoById(pedidoId)
                    .orElse(null);
            
            if (pedido == null) {
                return false;
            }

            // Solo se puede generar factura para pedidos completados
            return "COMPLETADO".equals(pedido.getEstado());
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public String obtenerEstadoFactura(Integer pedidoId) {
        try {
            Pedido pedido = pedidoService.getPedidoById(pedidoId).orElse(null);
            if (pedido == null) {
                return "Pedido no encontrado";
            }

            switch (pedido.getEstado()) {
                case "COMPLETADO":
                    return "Factura disponible";
                case "EN_PROCESO":
                    return "En proceso de preparación";
                case "PENDIENTE":
                    return "Pendiente de confirmación";
                case "CANCELADO":
                    return "Pedido cancelado";
                default:
                    return "Estado desconocido";
            }
        } catch (Exception e) {
            return "Error consultando estado";
        }
    }
}