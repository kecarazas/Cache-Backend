package cl.cache.cache_backend.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

//Crea la tabla de producto
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Codigo que ve el admin
    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    //identificador que usa el frontend ye el carrito
    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 50)
    private String marca;

    //Insignia que muestra la tarjeta(etiqueta)
    @Column(length = 40)
    private String etiqueta;

    //Precio ClP
    @Column(nullable = false)
    private Integer precio;

    //Precio con rebaja
    private Integer precioOferta;

    @Column(nullable = false)
    private Integer stock;

    //Stock Critico
    @Column(nullable = false)
    private Integer stockCritico;

    //Ruta de la imagen
    @Column(length = 500)
    private String imagen;

    //Valoracion del producto (0 - 5)
    @Column(nullable = false)
    private double opiniones;

    @Column(nullable = false)
    private boolean destacado;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "producto_especificacion", joinColumns = @JoinColumn(name = "producto_id"))
    @OrderColumn(name = "orden")
    @Column(name = "texto", length = 300)
    @Builder.Default
    private List<String> especificaciones = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    //Reglas del negocio dependiendo de los datos del producto

    public boolean enOferta() {
        return precioOferta != null && precioOferta > 0 && precioOferta < precio;
    }

    //Precio real
    public int precioFinal() {
        return enOferta() ? precioOferta : precio;
    }

    public boolean critico() {
        return stock <= stockCritico;
    }

    //Texto del descuento / oferta
    public String descuentoTexto() {
        return enOferta() ? Math.round((precio - precioOferta) * 100.0 / precio) + "% OFF" : null;
    }
}
