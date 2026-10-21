package es.mariana.dweb.model.repositories;

import es.mariana.dweb.model.entities.Pokemon;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@Repository
public class PokemonRepository {

    private static final String POKEMON_API_URL = "https://pokeapi.co/api/v2";
    private static final int MAX_POKEMON_ID = 1025;

    private final RestTemplate restTemplate;
    private final Random random;

    public PokemonRepository() {
        this(new RestTemplate());
    }

    public PokemonRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.random = new Random();
    }

    public Pokemon findByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        return restTemplate.getForObject(POKEMON_API_URL + "/pokemon/" + normalizedName, Pokemon.class);
    }

    public Pokemon findRandom() {
        int randomId = random.nextInt(MAX_POKEMON_ID) + 1;
        return findById(randomId);
    }

    public Pokemon findById(int id) {
        if (id < 1 || id > MAX_POKEMON_ID) {
            throw new IllegalArgumentException("El id del pokémon debe estar entre 1 y " + MAX_POKEMON_ID);
        }

        return restTemplate.getForObject(POKEMON_API_URL + "/pokemon/" + id, Pokemon.class);
    }

    public ArrayList<Pokemon> findRandomList(int limit) {
        ArrayList<Pokemon> result = new ArrayList<>();
        if (limit <= 0) {
            return result;
        }

        Set<Integer> generatedIds = new HashSet<>();
        while (generatedIds.size() < Math.min(limit, MAX_POKEMON_ID)) {
            generatedIds.add(random.nextInt(MAX_POKEMON_ID) + 1);
        }

        for (Integer id : generatedIds) {
            Pokemon pokemon = findById(id);
            if (pokemon != null) {
                result.add(pokemon);
            }
            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    public ArrayList<Pokemon> findAll() {
        return findRandomList(10);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Pokemon.Type> findAllTypes() {
        Map<String, Object> response = restTemplate.getForObject(
                POKEMON_API_URL + "/type/?limit=100", Map.class);
        ArrayList<Pokemon.Type> result = new ArrayList<>();
        if (response == null) return result;

        List<Map<String, String>> types =
                (List<Map<String, String>>) response.get("results");
        if (types == null) return result;

        for (Map<String, String> entry : types) {
            String name = entry.get("name");
            if ("unknown".equals(name) || "stellar".equals(name)) continue;
            Pokemon.Type type = new Pokemon.Type();
            type.setName(name);
            type.setUrl(entry.get("url"));
            result.add(type);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Pokemon> findByType(String typeName, int limit) {
        ArrayList<Pokemon> result = new ArrayList<>();
        if (typeName == null || typeName.isBlank() || limit <= 0) return result;

        String normalized = typeName.trim().toLowerCase(Locale.ROOT);
        Map<String, Object> response = restTemplate.getForObject(
                POKEMON_API_URL + "/type/" + normalized, Map.class);
        if (response == null) return result;

        List<Map<String, Object>> entries =
                (List<Map<String, Object>>) response.get("pokemon");
        if (entries == null) return result;

        for (Map<String, Object> entry : entries) {
            if (result.size() >= limit) break;
            Map<String, String> reference =
                    (Map<String, String>) entry.get("pokemon");
            if (reference == null) continue;
            String name = reference.get("name");
            if (name == null) continue;
            Pokemon pokemon = findByName(name);
            if (pokemon != null) result.add(pokemon);
        }
        return result;
    }
}

