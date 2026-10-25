package es.mariana.dweb.model.service;


import es.mariana.dweb.exception.EntrenadorException;
import es.mariana.dweb.model.entities.Entrenador;
import es.mariana.dweb.model.repositories.EntrenadorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
/* Centraliza las reglas de negocio y coordina los cambios persistentes. */
public class EntrenadorService {
    public static final List<String> TIPOS = List.of("Novato", "Entrenador", "Líder de gimnasio", "Alto Mando", "Campeón");
    private final EntrenadorRepository repository;
    public EntrenadorService(EntrenadorRepository repository) { this.repository=repository; }
    public List<Entrenador> findAll() { return repository.findAll(); }
    public Entrenador findById(int id) {
        return repository.findById(id).orElseThrow(()->new EntrenadorException("No existe el entrenador solicitado."));
    }
    private String validar(String nombre,String tipo,Integer id) {
        String limpio=nombre==null?"":nombre.trim();
        if(limpio.isEmpty())throw new EntrenadorException("El nombre es obligatorio.");
        if(limpio.length()>60)throw new EntrenadorException("El nombre no puede superar 60 caracteres.");
        if(!TIPOS.contains(tipo))throw new EntrenadorException("Selecciona un tipo válido.");
        if(repository.nombreExiste(limpio,id))throw new EntrenadorException("Ya existe un entrenador con ese nombre.");
        return limpio;
    }
    public Entrenador crear(String nombre,String tipo) {
        return repository.insertar(validar(nombre,tipo,null),tipo);
    }
    public Entrenador editar(int id,String nombre,String tipo) {
        findById(id);
        repository.actualizar(id,validar(nombre,tipo,id),tipo);
        return findById(id);
    }
    public void eliminar(int id) { findById(id); repository.eliminar(id); }
    public Entrenador anadirPokemon(int entrenadorId,int pokemonId) {
        Entrenador e=findById(entrenadorId);
        if(pokemonId<=0)throw new EntrenadorException("El identificador Pokémon no es válido.");
        if(e.getEquipo().contains(pokemonId))throw new EntrenadorException("Este Pokémon ya está en el equipo.");
        if(e.getEquipo().size()>=6)throw new EntrenadorException("El equipo ya tiene seis Pokémon.");
        repository.anadirPokemon(entrenadorId,pokemonId);
        return findById(entrenadorId);
    }
    public Entrenador quitarPokemon(int entrenadorId,int pokemonId) {
        Entrenador e=findById(entrenadorId);
        if(!e.getEquipo().contains(pokemonId))throw new EntrenadorException("El Pokémon no pertenece al equipo.");
        repository.quitarPokemon(entrenadorId,pokemonId);
        return findById(entrenadorId);
    }
}
