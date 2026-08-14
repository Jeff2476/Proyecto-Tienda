package com.tienda.controller;

import com.tienda.domain.Usuario;
import com.tienda.service.UsuarioService;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/usuario_rol")
public class UsuarioRolController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("rolesAsignados", Collections.emptySet());
        model.addAttribute("rolesDisponibles", Collections.emptyList());
        return "usuario_rol/mantenimiento";
    }

    @GetMapping("/buscar")
    public String buscarUsuario(@RequestParam("username") String username, Model model) {
        Usuario usuario = usuarioService.getUsuarioPorUsername(username).orElse(null);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            List<String> todosRolesNombres = usuarioService.getRolesNombres();
            List<String> rolesDisponibles = todosRolesNombres.stream()
                .filter(rolNombre -> usuario.getRoles().stream()
                        .noneMatch(rolAsignado -> rolAsignado.getRol().equals(rolNombre)))
                .toList();
            model.addAttribute("rolesAsignados", usuario.getRoles());
            model.addAttribute("rolesDisponibles", rolesDisponibles);
        }

        return "usuario_rol/mantenimiento";
    }

    @GetMapping("/agregar")
    public String agregarRol(@RequestParam("username") String username,
                             @RequestParam("nombreRol") String nombreRol) {
        usuarioService.asignarRolPorUsername(username, nombreRol);
        return "redirect:/usuario_rol/buscar?username=" + username;
    }

    @GetMapping("/eliminar")
    public String eliminarRol(@RequestParam("username") String username,
                              @RequestParam("idRol") Integer idRol) {
        usuarioService.eliminarRol(username, idRol);
        return "redirect:/usuario_rol/buscar?username=" + username;
    }
}
