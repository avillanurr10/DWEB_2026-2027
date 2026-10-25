package es.mariana.dweb.model.repositories;


import es.mariana.dweb.model.entities.Entrenador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
/* Lee y escribe entrenadores y equipos en la base de datos SQLite. */
public class EntrenadorRepository {
    private final String url;
    public EntrenadorRepository(@Value("${pokemon.sqlite.url:jdbc:sqlite:./pokemon.db}") String url) {
        this.url = url;
    }
    private Connection abrir() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) { s.execute("PRAGMA foreign_keys=ON"); }
        return c;
    }
    private Entrenador mapear(ResultSet rs, Connection c) throws SQLException {
        int id = rs.getInt("id");
        List<Integer> equipo = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("SELECT pokemon_id FROM equipos_pokemon WHERE entrenador_id=? ORDER BY pokemon_id")) {
            p.setInt(1,id);
            try (ResultSet r = p.executeQuery()) { while(r.next()) equipo.add(r.getInt(1)); }
        }
        return new Entrenador(id,rs.getString("nombre"),rs.getString("tipo"),
            LocalDate.parse(rs.getString("fecha_registro")),equipo);
    }
    public List<Entrenador> findAll() {
        List<Entrenador> lista = new ArrayList<>();
        try (Connection c=abrir(); PreparedStatement p=c.prepareStatement("SELECT * FROM entrenadores ORDER BY nombre COLLATE NOCASE"); ResultSet rs=p.executeQuery()) {
            while(rs.next()) lista.add(mapear(rs,c));
            return lista;
        } catch(SQLException e) { throw new IllegalStateException("Error leyendo entrenadores",e); }
    }
    public Optional<Entrenador> findById(int id) {
        try(Connection c=abrir(); PreparedStatement p=c.prepareStatement("SELECT * FROM entrenadores WHERE id=?")) {
            p.setInt(1,id);
            try(ResultSet rs=p.executeQuery()) { return rs.next()?Optional.of(mapear(rs,c)):Optional.empty(); }
        } catch(SQLException e) { throw new IllegalStateException("Error leyendo entrenador",e); }
    }
    public boolean nombreExiste(String nombre, Integer excluirId) {
        String sql="SELECT COUNT(*) FROM entrenadores WHERE nombre=? COLLATE NOCASE"+(excluirId==null?"":" AND id<>?");
        try(Connection c=abrir(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,nombre); if(excluirId!=null)p.setInt(2,excluirId);
            try(ResultSet rs=p.executeQuery()) { return rs.next()&&rs.getInt(1)>0; }
        } catch(SQLException e) { throw new IllegalStateException("Error validando nombre",e); }
    }
    public Entrenador insertar(String nombre,String tipo) {
        try(Connection c=abrir(); PreparedStatement p=c.prepareStatement(
                "INSERT INTO entrenadores(nombre,tipo,fecha_registro) VALUES(?,?,?)")) {
            p.setString(1,nombre);p.setString(2,tipo);p.setString(3,LocalDate.now().toString());p.executeUpdate();
            try(Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT last_insert_rowid()")) {
                rs.next();return findById(rs.getInt(1)).orElseThrow();
            }
        } catch(SQLException e) { throw new IllegalStateException("No se pudo crear el entrenador",e); }
    }
    public void actualizar(int id,String nombre,String tipo) {
        try(Connection c=abrir(); PreparedStatement p=c.prepareStatement("UPDATE entrenadores SET nombre=?,tipo=? WHERE id=?")) {
            p.setString(1,nombre);p.setString(2,tipo);p.setInt(3,id);p.executeUpdate();
        } catch(SQLException e) { throw new IllegalStateException("No se pudo actualizar el entrenador",e); }
    }
    public void eliminar(int id) {
        try(Connection c=abrir(); PreparedStatement p=c.prepareStatement("DELETE FROM entrenadores WHERE id=?")) {
            p.setInt(1,id);p.executeUpdate();
        } catch(SQLException e) { throw new IllegalStateException("No se pudo eliminar el entrenador",e); }
    }
    public void anadirPokemon(int entrenadorId,int pokemonId) {
        try(Connection c=abrir(); PreparedStatement p=c.prepareStatement("INSERT INTO equipos_pokemon(entrenador_id,pokemon_id) VALUES(?,?)")) {
            p.setInt(1,entrenadorId);p.setInt(2,pokemonId);p.executeUpdate();
        } catch(SQLException e) { throw new IllegalStateException("No se pudo añadir el Pokémon",e); }
    }
    public void quitarPokemon(int entrenadorId,int pokemonId) {
        try(Connection c=abrir(); PreparedStatement p=c.prepareStatement("DELETE FROM equipos_pokemon WHERE entrenador_id=? AND pokemon_id=?")) {
            p.setInt(1,entrenadorId);p.setInt(2,pokemonId);p.executeUpdate();
        } catch(SQLException e) { throw new IllegalStateException("No se pudo eliminar el Pokémon",e); }
    }
}
