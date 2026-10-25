package es.mariana.dweb.model.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/* Representa al entrenador; su equipo contiene los IDs de sus Pokémon. */
public class Entrenador {
    private Integer id;
    private String nombre;
    private String tipo;
    private LocalDate fechaRegistro;
    private List<Integer> equipo = new ArrayList<>();

    public Entrenador() {}
    public Entrenador(Integer id, String nombre, String tipo, LocalDate fechaRegistro, List<Integer> equipo) {
        this.id = id; this.nombre = nombre; this.tipo = tipo; this.fechaRegistro = fechaRegistro;
        this.equipo = new ArrayList<>(equipo);
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public List<Integer> getEquipo() { return equipo; }
    public void setEquipo(List<Integer> equipo) { this.equipo = new ArrayList<>(equipo); }
}
