package co.edu.uniquindio.retos.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "saludo_registros")
public class SaludoRegistro {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private OffsetDateTime fechaHora;

    public SaludoRegistro() {}
    public SaludoRegistro(String nombre, OffsetDateTime fechaHora) {
        this.nombre = nombre;
        this.fechaHora = fechaHora;
    }
    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public OffsetDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(OffsetDateTime fechaHora) { this.fechaHora = fechaHora; }
}
