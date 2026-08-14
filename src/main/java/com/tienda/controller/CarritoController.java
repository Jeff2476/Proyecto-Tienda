package com.tienda.controller;

import com.tienda.domain.Item;
import com.tienda.domain.Factura;
import com.tienda.domain.Usuario;
import com.tienda.service.CarritoService;
import com.tienda.service.FacturaService;
import com.tienda.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CarritoController {

    private final CarritoService carritoService;
    private final UsuarioService usuarioService;
    private final FacturaService facturaService;

    public CarritoController(CarritoService carritoService, UsuarioService usuarioService, FacturaService facturaService) {
        this.carritoService = carritoService;
        this.usuarioService = usuarioService;
        this.facturaService = facturaService;
    }

    @GetMapping("/carrito/listado")
    public String listado(HttpSession session, Model model) {
        List<Item> carrito = carritoService.obtenerCarrito(session);
        model.addAttribute("carritoItems", carrito);
        model.addAttribute("totalCarrito", carritoService.calcularTotal(carrito));
        return "/carrito/listado";
    }

    @PostMapping("/carrito/agregar")
    public ModelAndView agregar(
            @RequestParam("idProducto") Integer idProducto,
            HttpSession session,
            Model model) {
        try {
            System.out.println("Entro al carrito");
            List<Item> carrito = carritoService.obtenerCarrito(session);
            carritoService.agregarProducto(carrito, idProducto);
            carritoService.guardarCarrito(session, carrito);
            model.addAttribute("carritoTotal", carritoService.calcularTotal(carrito));
            model.addAttribute("listaItems", carrito);
            return new ModelAndView("/carrito/fragmentos :: verCarrito", model.asMap());
        } catch (RuntimeException e) {
            model.addAttribute("errorMensaje", e.getMessage());
            return new ModelAndView("/errores/fragmentos :: errorMensaje", model.asMap());
        }
    }

    @PostMapping("/carrito/eliminar/{idProducto}")
    public String eliminarItem(
            @PathVariable("idProducto") Integer idProducto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        List<Item> carrito = carritoService.obtenerCarrito(session);
        carritoService.eliminarItem(carrito, idProducto);
        carritoService.guardarCarrito(session, carrito);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito.");
        return "redirect:/carrito/listado";
    }

    @GetMapping("/carrito/modificar/{idProducto}")
    public String modificar(
            @PathVariable("idProducto") Integer idProducto,
            HttpSession session,
            Model model) {
        List<Item> carrito = carritoService.obtenerCarrito(session);
        Item item = carritoService.buscarItem(carrito, idProducto);
        if (item == null) {
            System.out.println("Hubo problemas");
            return "redirect:/carrito/listado";
        }
        model.addAttribute("item", item);
        return "/carrito/modifica";
    }

    @PostMapping("/carrito/actualizar")
    public String actualizarCantidad(
            @RequestParam("producto.idProducto") Integer idProducto,
            @RequestParam("cantidad") int nuevaCantidad,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            List<Item> carrito = carritoService.obtenerCarrito(session);
            carritoService.actualizarCantidad(carrito, idProducto, nuevaCantidad);
            carritoService.guardarCarrito(session, carrito);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito/listado";
    }

    @GetMapping("/facturar/carrito")
    public String facturarCarrito(HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("Va a facturar");
        try {
            List<Item> carrito = carritoService.obtenerCarrito(session);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            System.out.println("El username es:"+username);
            Usuario usuario = usuarioService.getUsuarioPorUsername(username).get();
            Factura factura = carritoService.procesarCompra(carrito, usuario);
            carritoService.limpiarCarrito(session);
            redirectAttributes.addFlashAttribute("idFactura", factura.getIdFactura());
            redirectAttributes.addFlashAttribute("mensaje", "Compra procesada con éxito. Factura Nro: " + factura.getIdFactura());
            System.out.println("Ver la Factura");
            return "redirect:/carrito/verFactura";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "redirect:/carrito/listado";
        }
    }

    @GetMapping("/carrito/verFactura")
    public String verFactura(@ModelAttribute("idFactura") Integer idFactura, Model model) {
        if (idFactura == null) {
            return "redirect:/index";
        }
        Factura factura = facturaService.getFacturaConVentas(idFactura);
        model.addAttribute("factura", factura);
        return "/carrito/verFactura";
    }
}
