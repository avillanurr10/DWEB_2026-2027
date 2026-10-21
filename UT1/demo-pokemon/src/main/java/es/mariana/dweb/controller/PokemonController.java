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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
