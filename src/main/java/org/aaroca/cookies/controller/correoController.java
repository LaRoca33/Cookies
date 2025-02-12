package org.aaroca.cookies.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aaroca.cookies.POJO.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@Controller
@RequestMapping("/correo")
class CorreoController {
    private static final Map<String, Usuario> usuarios = new HashMap<>();

    static {
        usuarios.put("usuario1", new Usuario("usuario1", "password123"));
        usuarios.put("usuario2", new Usuario("usuario2", "clave456"));
    }

    @GetMapping("usuario")
    public String mostrarPaginaInicio(HttpServletRequest request, HttpServletResponse response, Model model) {
        Cookie[] cookies = request.getCookies();
        String usuario = null;
        int contador = 0;
        boolean desconectado = true;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("usuario".equals(cookie.getName())) {
                    usuario = cookie.getValue();
                } else if ("contador".equals(cookie.getName())) {
                    contador = Integer.parseInt(cookie.getValue());
                } else if ("desconectado".equals(cookie.getName())) {
                    desconectado = Boolean.parseBoolean(cookie.getValue());
                }
            }
        }

        if (usuario == null || usuario.isEmpty()) {
            model.addAttribute("usuario", new Usuario("", ""));  // Usuario vacío
            return "solicitarUsuario";
        } else if (desconectado) {
            model.addAttribute("usuario", new Usuario(usuario, ""));  // Usuario con nombre y contraseña vacíos
            return "solicitarContraseña";
        } else {
            contador++;
            response.addCookie(new Cookie("usuario", usuario));
            response.addCookie(new Cookie("contador", String.valueOf(contador)));
            model.addAttribute("usuario", usuario);
            model.addAttribute("contador", contador);
            return "areaPersonal";
        }
    }

    @GetMapping("/validarUsuario")
    public String evitarSalto(){
        return "redirect:/correo/usuario";
    }

    @PostMapping("/validarUsuario")
    public String validarUsuario(@ModelAttribute Usuario usuario, HttpServletResponse response, Model model) {
        if (usuarios.containsKey(usuario.getNombre())) {
            response.addCookie(new Cookie("usuario", usuario.getNombre()));
            response.addCookie(new Cookie("contador", "0"));
            response.addCookie(new Cookie("desconectado", "true"));
            model.addAttribute("usuario", usuario);
            return "solicitarContraseña";
        } else {
            model.addAttribute("error", "Usuario no encontrado");
            return "solicitarUsuario";
        }
    }

    @PostMapping("/validarContraseña")
    public String validarContraseña(@ModelAttribute Usuario usuario, HttpServletRequest request, HttpServletResponse response, Model model) {
        Cookie[] cookies = request.getCookies();
        int contador = 0;
        boolean usuarioValido = false;

        for (Cookie cookie : cookies) {
            if ("usuario".equals(cookie.getName()) && cookie.getValue().equals(usuario.getNombre())) {
                usuarioValido = true;
            } else if ("contador".equals(cookie.getName())) {
                contador = Integer.parseInt(cookie.getValue());
            }
        }

        if (!usuarioValido) {
            return "redirect:/correo/usuario";
        }

        Usuario user = usuarios.get(usuario.getNombre());
        if (user != null && user.getContraseña().equals(usuario.getContraseña())) {
            contador++;
            response.addCookie(new Cookie("usuario", usuario.getNombre()));
            response.addCookie(new Cookie("contador", String.valueOf(contador)));
            response.addCookie(new Cookie("desconectado", "false"));
            model.addAttribute("usuario", usuario.getNombre());
            model.addAttribute("contador", contador);
            return "areaPersonal";
        } else {
            model.addAttribute("error", "Contraseña incorrecta");
            model.addAttribute("usuario", usuario);
            return "solicitarContraseña";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addCookie(new Cookie("desconectado", "true"));
        return "redirect:/correo/usuario";
    }
}
