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
            return "solicitarUsuario";
        } else if (desconectado) {
            model.addAttribute("usuario", usuario);
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
    public String validarUsuario(@RequestParam String usuario, HttpServletResponse response, Model model) {
        if (usuarios.containsKey(usuario)) {
            response.addCookie(new Cookie("usuario", usuario));
            response.addCookie(new Cookie("contador", "0"));
            response.addCookie(new Cookie("desconectado", "true"));
            model.addAttribute("usuario", usuario);
            return "solicitarContraseña";
        } else {
            model.addAttribute("error", "Usuario no encontrado");
            return "redirect:/correo/usuario";
        }
    }

    @GetMapping("/validarContraseña")
    public String evitarSalto2(){
        return "redirect:/correo/usuario";
    }


    @PostMapping("/validarContraseña")
    public String validarContraseña(@RequestParam String usuario, @RequestParam String contraseña, HttpServletRequest request, HttpServletResponse response, Model model) {
        Cookie[] cookies = request.getCookies();
        int contador = 0;
        boolean usuarioValido = false;

        for (Cookie cookie : cookies) {
            if ("usuario".equals(cookie.getName()) && cookie.getValue().equals(usuario)) {
                usuarioValido = true;
            } else if ("contador".equals(cookie.getName())) {
                contador = Integer.parseInt(cookie.getValue());
            }
        }

        if (!usuarioValido) {
            return "redirect:/correo/usuario";
        }

        Usuario user = usuarios.get(usuario);
        if (user != null && user.getContraseña().equals(contraseña)) {
            contador++;
            response.addCookie(new Cookie("usuario", usuario));
            response.addCookie(new Cookie("contador", String.valueOf(contador)));
            response.addCookie(new Cookie("desconectado", "false"));
            model.addAttribute("usuario", usuario);
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