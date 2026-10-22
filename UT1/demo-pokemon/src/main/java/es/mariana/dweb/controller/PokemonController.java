package es.mariana.dweb.controller;

import es.mariana.dweb.model.entities.Pokemon;
import es.mariana.dweb.model.entities.PokemonHeightComparator;
import es.mariana.dweb.model.entities.PokemonNameComparator;
import es.mariana.dweb.model.entities.PokemonWeightComparator;
import es.mariana.dweb.model.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

@Controller
public class PokemonController {

    /*/pokemon/6 → Controller → Service → Repository → PokéAPI
                                     ← Pokemon ←  */
    @Autowired
    private PokemonService pokemonService;

    @ModelAttribute("entrenador")
    public String entrenadorEnMenu(HttpSession session) {
        return (String) session.getAttribute("entrenador");
    }

    @GetMapping("/")
    public String home(Model model) {
        ArrayList<Pokemon> pokemons = pokemonService.obtenerPokemons();
        model.addAttribute("pokemons", pokemons);

        return "home";
    }
    @GetMapping("/pokemon/tabla")
    public String listadoTabla(
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "alturaMin", required = false) Double alturaMin,
            @RequestParam(name = "pesoMax", required = false) Double pesoMax,
            @RequestParam(name = "orden", required = false) String orden,
            @RequestParam(name = "direccion", required = false) String direccion,
            Model model) {
        ArrayList<Pokemon> base = (tipo == null || tipo.isBlank())
                ? pokemonService.findRandomList(10)
                : pokemonService.findByType(tipo);
        ArrayList<Pokemon> pokemons = filtrarYOrdenar(base, nombre, alturaMin, pesoMax, orden, direccion);
        model.addAttribute("pokemons", pokemons);
        model.addAttribute("titulo", "Pokémon en formato tabla");
        model.addAttribute("fechaHora", LocalDateTime.now());
        agregarFiltrosAlModelo(model, tipo, nombre, alturaMin, pesoMax, orden, direccion, "/pokemon/tabla");

        return "listado-tabla";
    }

    @GetMapping("/pokemon/galeria")
    public String galeria(
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "alturaMin", required = false) Double alturaMin,
            @RequestParam(name = "pesoMax", required = false) Double pesoMax,
            @RequestParam(name = "orden", required = false) String orden,
            @RequestParam(name = "direccion", required = false) String direccion,
            Model model) {
        ArrayList<Pokemon> base = (tipo == null || tipo.isBlank())
                ? pokemonService.findRandomList(12)
                : pokemonService.findByType(tipo);
        ArrayList<Pokemon> pokemons = filtrarYOrdenar(base, nombre, alturaMin, pesoMax, orden, direccion);
        model.addAttribute("pokemons", pokemons);
        model.addAttribute("titulo", "Galería de Pokémon");
        agregarFiltrosAlModelo(model, tipo, nombre, alturaMin, pesoMax, orden, direccion, "/pokemon/galeria");
        return "galeria-pokemon";
    }
    @GetMapping("/pokemon/equipo")
    public String equipo(HttpSession session, Model model) {
        String entrenador = (String) session.getAttribute("entrenador");
        model.addAttribute("entrenador", entrenador);
        ArrayList<Pokemon> equipo = obtenerEquipo(session);
        model.addAttribute("equipo", equipo);
        return "equipo";
    }
    @PostMapping("/pokemon/entrenador")
    public String registrarEntrenador(
            @RequestParam(name = "nombre", required = false) String nombre,
            HttpSession session,
            Model model) {

        if (nombre == null || nombre.isBlank()) {
            model.addAttribute("entrenador", null);
            model.addAttribute("equipo", new ArrayList<Pokemon>());
            model.addAttribute("errorRegistro", "Escribe un nombre para el entrenador.");
            return "equipo";
        }

        session.setAttribute("entrenador", nombre.trim());
        session.setAttribute("equipo", new ArrayList<Pokemon>());

        return "redirect:/pokemon/equipo";
    }

    @PostMapping("/pokemon/equipo/agregar")
    public String agregarPokemon(
            @RequestParam int id,
            @RequestParam String origen,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "alturaMin", required = false) Double alturaMin,
            @RequestParam(name = "pesoMax", required = false) Double pesoMax,
            @RequestParam(name = "orden", required = false) String orden,
            @RequestParam(name = "direccion", required = false) String direccion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("entrenador") == null) {
            return "redirect:/pokemon/equipo";
        }

        ArrayList<Pokemon> equipo = obtenerEquipo(session);
        if (equipo.stream().anyMatch(pokemon -> pokemon.getId() == id)) {
            redirectAttributes.addFlashAttribute("mensajeError", "Este Pokémon ya pertenece a tu equipo.");
        } else if (equipo.size() >= 6) {
            redirectAttributes.addFlashAttribute("mensajeError", "Tu equipo ya tiene seis Pokémon.");
        } else {
            equipo.add(pokemonService.findById(id));
        }

        return redireccionListado(origen, tipo, nombre, alturaMin, pesoMax, orden, direccion);
    }

    @PostMapping("/pokemon/equipo/eliminar")
    public String eliminarPokemon(@RequestParam int id, HttpSession session) {
        if (session.getAttribute("entrenador") != null) {
            obtenerEquipo(session).removeIf(pokemon -> pokemon.getId() == id);
        }
        return "redirect:/pokemon/equipo";
    }

    @GetMapping("/pokemon/equipo/combate/{id}")
    public String batallaDesdeEquipo(@PathVariable int id, HttpSession session, Model model) {
        if (session.getAttribute("entrenador") == null
                || obtenerEquipo(session).stream().noneMatch(pokemon -> pokemon.getId() == id)) {
            return "redirect:/pokemon/equipo";
        }

        Pokemon pokemon1 = pokemonService.findById(id);
        Pokemon pokemon2 = pokemonService.findRandom();
        model.addAttribute("pokemon1", pokemon1);
        model.addAttribute("pokemon2", pokemon2);
        return "batalla-pokemon";
    }

    @PostMapping("/pokemon/entrenador/salir")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/pokemon/equipo";
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Pokemon> obtenerEquipo(HttpSession session) {
        ArrayList<Pokemon> equipo = (ArrayList<Pokemon>) session.getAttribute("equipo");
        if (equipo == null) {
            equipo = new ArrayList<>();
            if (session.getAttribute("entrenador") != null) {
                session.setAttribute("equipo", equipo);
            }
        }
        return equipo;
    }

    private String redireccionListado(String origen, String tipo, String nombre,
                                      Double alturaMin, Double pesoMax,
                                      String orden, String direccion) {
        String ruta = "/pokemon/galeria".equals(origen) ? "/pokemon/galeria" : "/pokemon/tabla";
        UriComponentsBuilder url = UriComponentsBuilder.fromPath(ruta);
        if (tipo != null && !tipo.isBlank()) url.queryParam("tipo", tipo);
        if (nombre != null && !nombre.isBlank()) url.queryParam("nombre", nombre);
        if (alturaMin != null) url.queryParam("alturaMin", alturaMin);
        if (pesoMax != null) url.queryParam("pesoMax", pesoMax);
        if (orden != null && !orden.isBlank()) url.queryParam("orden", orden);
        if (direccion != null && !direccion.isBlank()) url.queryParam("direccion", direccion);
        return "redirect:" + url.build().encode().toUriString();
    }

    private ArrayList<Pokemon> filtrarYOrdenar(ArrayList<Pokemon> pokemons, String nombre,
                                               Double alturaMin, Double pesoMax,
                                               String orden, String direccion) {
        ArrayList<Pokemon> resultado = new ArrayList<>(pokemons);

        if (nombre != null && !nombre.isBlank()) {
            String nombreBuscado = nombre.trim().toLowerCase(Locale.ROOT);
            resultado.removeIf(pokemon -> pokemon.getName() == null
                    || !pokemon.getName().toLowerCase(Locale.ROOT).contains(nombreBuscado));
        }
        if (alturaMin != null) {
            resultado.removeIf(pokemon -> pokemon.getHeight() / 10.0 < alturaMin);
        }
        if (pesoMax != null) {
            resultado.removeIf(pokemon -> pokemon.getWeight() / 10.0 > pesoMax);
        }

        Comparator<Pokemon> comparador = null;
        if ("name".equalsIgnoreCase(orden)) {
            comparador = new PokemonNameComparator();
        } else if ("height".equalsIgnoreCase(orden)) {
            comparador = new PokemonHeightComparator();
        } else if ("weight".equalsIgnoreCase(orden)) {
            comparador = new PokemonWeightComparator();
        }

        if (comparador != null) {
            if ("desc".equalsIgnoreCase(direccion)) {
                comparador = comparador.reversed();
            }
            resultado.sort(comparador);
        }
        return resultado;
    }

    private void agregarFiltrosAlModelo(Model model, String tipo, String nombre,
                                        Double alturaMin, Double pesoMax,
                                        String orden, String direccion, String formAction) {
        model.addAttribute("tipos", pokemonService.findAllTypes());
        model.addAttribute("tipo", tipo == null ? "" : tipo);
        model.addAttribute("nombre", nombre == null ? "" : nombre);
        model.addAttribute("alturaMin", alturaMin);
        model.addAttribute("pesoMax", pesoMax);
        model.addAttribute("orden", orden == null ? "" : orden);
        model.addAttribute("direccion", direccion == null || direccion.isBlank() ? "asc" : direccion);
        model.addAttribute("formAction", formAction);
    }

    @GetMapping("/pokemon/batalla")
    public String batalla(Model model) {
        Pokemon pokemon1 = pokemonService.findRandom();
        Pokemon pokemon2 = pokemonService.findRandom();
        model.addAttribute("pokemon1", pokemon1);
        model.addAttribute("pokemon2", pokemon2);
        return "batalla-pokemon";
    }

    @GetMapping("/informacion")
    public String informacion(Model model) {
        Pokemon pokemon = pokemonService.findRandom();
        model.addAttribute("titulo", "Pokémon destacado");
        model.addAttribute("presentacion", "Conoce los datos principales de uno de los Pokémon disponibles en nuestra Pokédex.");
        model.addAttribute("fechaHora", LocalDateTime.now());
        model.addAttribute("pokemon", pokemon);

        return "informacion";
    }

    @GetMapping("/pokemon/{id}")
    public String detallePokemon(@PathVariable("id") int id, Model model) {
        Pokemon pokemon = pokemonService.findById(id);
        model.addAttribute("titulo", "Detalle de Pokémon");
        model.addAttribute("presentacion", "Consulta los datos principales de este Pokémon.");
        model.addAttribute("fechaHora", LocalDateTime.now());
        model.addAttribute("pokemon", pokemon);
        return "informacion";
    }

}
