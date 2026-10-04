package es.mariana.dweb.controller;

import es.mariana.dweb.model.entities.Pokemon;
import es.mariana.dweb.model.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/")
    public String home(Model model) {
        ArrayList<Pokemon> pokemons = pokemonService.obtenerPokemons();
        model.addAttribute("pokemons", pokemons);

        return "home";
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

}
