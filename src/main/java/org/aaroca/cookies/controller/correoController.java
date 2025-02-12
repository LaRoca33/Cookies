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

    // Mapa estático de usuarios simulados (por ahora, solo hay dos usuarios predefinidos)
    private static final Map<String, Usuario> usuarios = new HashMap<>();

    // Inicialización estática de los usuarios simulados
    static {
        usuarios.put("usuario1", new Usuario("usuario1", "password123"));
        usuarios.put("usuario2", new Usuario("usuario2", "clave456"));
    }

    // Mapeo del GET a la ruta "/correo/usuario", que muestra la página de inicio
    @GetMapping("usuario")
    public String mostrarPaginaInicio(HttpServletRequest request, HttpServletResponse response, Model model) {
        // Obtener las cookies de la solicitud (si existen)
        Cookie[] cookies = request.getCookies();
        String usuario = null;
        int contador = 0;
        boolean desconectado = true;

        // Iterar sobre las cookies para obtener valores específicos
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("usuario".equals(cookie.getName())) {  // Si la cookie es "usuario", guardamos su valor
                    usuario = cookie.getValue();
                } else if ("contador".equals(cookie.getName())) {  // Si la cookie es "contador", guardamos su valor
                    contador = Integer.parseInt(cookie.getValue());
                } else if ("desconectado".equals(cookie.getName())) {  // Si la cookie es "desconectado", guardamos su valor
                    desconectado = Boolean.parseBoolean(cookie.getValue());
                }
            }
        }

        // Si no hay un usuario almacenado en las cookies (usuario es null o vacío), mostramos el formulario de usuario
        if (usuario == null || usuario.isEmpty()) {
            model.addAttribute("usuario", new Usuario("", ""));  // Creamos un objeto Usuario vacío para mostrar en la vista
            return "solicitarUsuario";
        } else if (desconectado) {  // Si el usuario está desconectado, le pedimos la contraseña
            model.addAttribute("usuario", new Usuario(usuario, ""));  // Enviamos el nombre de usuario a la vista
            return "solicitarContraseña";
        } else {  // Si el usuario está conectado, mostramos su área personal
            contador++;  // Incrementamos el contador de accesos
            response.addCookie(new Cookie("usuario", usuario));  // Volvemos a poner la cookie "usuario"
            response.addCookie(new Cookie("contador", String.valueOf(contador)));  // Actualizamos el contador en la cookie
            model.addAttribute("usuario", usuario);  // Enviamos el nombre de usuario a la vista
            model.addAttribute("contador", contador);  // Enviamos el contador a la vista
            return "areaPersonal";  // Redirigimos a la vista del área personal
        }
    }

    // Método GET que redirige a la página de inicio, evita que se acceda a "validarUsuario" por GET
    @GetMapping("/validarUsuario")
    public String evitarSalto(){
        return "redirect:/correo/usuario";  // Redirige a la página de inicio de sesión para evitar acceso no deseado
    }

    // Método POST que valida si el usuario existe en el mapa de usuarios
    @PostMapping("/validarUsuario")
    public String validarUsuario(@ModelAttribute Usuario usuario, HttpServletResponse response, Model model) {
        if (usuarios.containsKey(usuario.getNombre())) {  // Verificamos si el usuario existe en el mapa
            response.addCookie(new Cookie("usuario", usuario.getNombre()));  // Guardamos el nombre de usuario en la cookie
            response.addCookie(new Cookie("contador", "0"));  // Inicializamos el contador en 0
            response.addCookie(new Cookie("desconectado", "true"));  // Marcamos como desconectado al usuario
            model.addAttribute("usuario", usuario);  // Enviamos el usuario a la vista de contraseña
            return "solicitarContraseña";  // Redirigimos a la vista de ingreso de contraseña
        } else {  // Si el usuario no existe, mostramos un mensaje de error
            model.addAttribute("error", "Usuario no encontrado");  // Agregamos el mensaje de error
            return "solicitarUsuario";  // Redirigimos a la vista de inicio de sesión
        }
    }

    // Método POST que valida la contraseña ingresada por el usuario
    @PostMapping("/validarContraseña")
    public String validarContraseña(@ModelAttribute Usuario usuario, @RequestParam String contraseña, HttpServletRequest request, HttpServletResponse response, Model model) {
        Cookie[] cookies = request.getCookies();  // Obtenemos las cookies de la solicitud
        int contador = 0;
        boolean usuarioValido = false;

        // Iteramos sobre las cookies para verificar la validez del usuario y el contador
        for (Cookie cookie : cookies) {
            if ("usuario".equals(cookie.getName()) && cookie.getValue().equals(usuario.getNombre())) {  // Verificamos si el nombre de usuario coincide
                usuarioValido = true;  // El usuario es válido si coincide el nombre
            } else if ("contador".equals(cookie.getName())) {  // Obtenemos el valor del contador
                contador = Integer.parseInt(cookie.getValue());
            }
        }

        // Si el usuario no es válido (no está presente en las cookies), redirigimos a la página de inicio
        if (!usuarioValido) {
            return "redirect:/correo/usuario";
        }

        // Verificamos si la contraseña ingresada es correcta
        Usuario user = usuarios.get(usuario.getNombre());
        if (user != null && user.getContraseña().equals(contraseña)) {  // Si la contraseña es correcta
            contador++;  // Incrementamos el contador de accesos
            response.addCookie(new Cookie("usuario", usuario.getNombre()));  // Guardamos el nombre de usuario en la cookie
            response.addCookie(new Cookie("contador", String.valueOf(contador)));  // Actualizamos el contador en la cookie
            response.addCookie(new Cookie("desconectado", "false"));  // Marcamos que el usuario ya no está desconectado
            model.addAttribute("usuario", usuario.getNombre());  // Enviamos el nombre de usuario a la vista
            model.addAttribute("contador", contador);  // Enviamos el contador a la vista
            return "areaPersonal";  // Redirigimos a la vista del área personal
        } else {  // Si la contraseña es incorrecta
            model.addAttribute("error", "Contraseña incorrecta");  // Enviamos un mensaje de error
            model.addAttribute("usuario", usuario);  // Enviamos el usuario a la vista para que se mantenga el campo de nombre
            return "solicitarContraseña";  // Redirigimos nuevamente a la vista de contraseña
        }
    }

    // Método POST que maneja el cierre de sesión
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addCookie(new Cookie("desconectado", "true"));  // Establecemos la cookie "desconectado" como true
        return "redirect:/correo/usuario";  // Redirigimos a la página de inicio de sesión
    }
}
