package cl.cache.cache_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Verificador del run
    @Column(nullable = false, unique = true, length = 12)
    private String run;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    //nombre del usuario guardado en minusculas
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    //Encripta la contraceña (hash bcrypt)
    @Column(nullable = false, length = 100)
    private String password;

    //Se almacena como texto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String comuna;

    @Column(length = 300)
    private String direccion;

    @Column(length = 50)
    private String departamento;

    private LocalDate fechaNacimiento;
}
