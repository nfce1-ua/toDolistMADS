package madstodolist.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "equipos")
public class Equipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // Constructor vacío necesario para JPA/Hibernate.
    // No debe usarse desde la aplicación.
    public Equipo() {}

    public Equipo(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipo equipo = (Equipo) o;
        if (id != null && equipo.id != null)
            // Si ambos tienen ID, comparamos por ID
            return Objects.equals(id, equipo.id);
        // Si alguno no tiene ID, comparamos por el nombre
        return Objects.equals(nombre, equipo.nombre);
    }

    @Override
    public int hashCode() {
        // Generamos un hash basado en el nombre (o en los campos principales)
        return Objects.hash(nombre);
    }
}