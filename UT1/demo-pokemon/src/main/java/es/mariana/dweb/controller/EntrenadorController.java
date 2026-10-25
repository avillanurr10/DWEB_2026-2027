package es.mariana.dweb.controller;

import es.mariana.dweb.exception.EntrenadorException;
import es.mariana.dweb.model.entities.Entrenador;
import es.mariana.dweb.model.service.EntrenadorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EntrenadorController {
    @Autowired
    private EntrenadorService servicio;

    // Prepara el listado y marca qué entrenador está seleccionado en la sesión.
    @GetMapping("/entrenadores")
    public String listado(Model model, HttpSession session) {
        Entrenador seleccionado = (Entrenador) session.getAttribute("entrenador");
        model.addAttribute("entrenadores", servicio.findAll());
        model.addAttribute("entrenadorSeleccionado", seleccionado);
        model.addAttribute("entrenador", seleccionado);
        return "entrenadores";
    }

    @PostMapping("/entrenadores/seleccionar")
    public String seleccionar(@RequestParam int id, HttpSession session) {
        session.setAttribute("entrenador", servicio.findById(id));
        return "redirect:/entrenadores";
    }

    @PostMapping("/entrenadores/salir")
    public String salir(HttpSession session) {
        session.removeAttribute("entrenador");
        return "redirect:/entrenadores";
    }

    // Prepara el formulario vacío para crear un entrenador.
    @GetMapping("/entrenadores/nuevo")
    public String nuevo(Model model, HttpSession session) {
        prepararFormulario(model, null, "", "", null);
        model.addAttribute("entrenador", session.getAttribute("entrenador"));
        return "formulario-entrenador";
    }

    // Sin ID crea; con ID edita. El servicio valida los datos en ambos casos.
    @PostMapping("/entrenadores/guardar")
    public String guardar(
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "tipo", required = false) String tipo,
            Model model,
            HttpSession session) {
        try {
            if (id == null) {
                servicio.crear(nombre, tipo);
            } else {
                Entrenador actualizado = servicio.editar(id, nombre, tipo);
                Entrenador seleccionado = (Entrenador) session.getAttribute("entrenador");
                // Si editamos al entrenador activo, reemplazamos el objeto antiguo de la sesión.
                if (seleccionado != null && seleccionado.getId().equals(id)) {
                    session.setAttribute("entrenador", actualizado);
                }
            }
            return "redirect:/entrenadores";
        } catch (EntrenadorException ex) {
            prepararFormulario(model, id, nombre, tipo, ex.getMessage());
            model.addAttribute("entrenador", session.getAttribute("entrenador"));
            return "formulario-entrenador";
        }
    }

    @GetMapping("/entrenadores/{id}/editar")
    public String editar(@org.springframework.web.bind.annotation.PathVariable int id,
                         Model model,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            Entrenador entrenador = servicio.findById(id);
            prepararFormulario(model, entrenador.getId(), entrenador.getNombre(), entrenador.getTipo(), null);
            model.addAttribute("entrenador", session.getAttribute("entrenador"));
            return "formulario-entrenador";
        } catch (EntrenadorException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/entrenadores";
        }
    }

    // El servicio borra al entrenador y SQLite elimina en cascada las filas de su equipo.
    @PostMapping("/entrenadores/eliminar")
    public String eliminar(@RequestParam int id, HttpSession session,RedirectAttributes redirectAttributes) {
        try {
            servicio.eliminar(id);
            Entrenador seleccionado = (Entrenador) session.getAttribute("entrenador");
            if (seleccionado != null && seleccionado.getId().equals(id)) {
                session.removeAttribute("entrenador");
            }
        } catch (EntrenadorException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/entrenadores";
    }

    // Carga los valores que necesita la misma plantilla para alta y edición.
    private void prepararFormulario(Model model, Integer id, String nombre, String tipo, String error) {
        model.addAttribute("id", id);
        model.addAttribute("nombre", nombre == null ? "" : nombre);
        model.addAttribute("tipo", tipo == null ? "" : tipo);
        model.addAttribute("tipos", EntrenadorService.TIPOS);
        model.addAttribute("tituloFormulario", id == null ? "Nuevo entrenador" : "Editar entrenador");
        model.addAttribute("error", error);
    }
}