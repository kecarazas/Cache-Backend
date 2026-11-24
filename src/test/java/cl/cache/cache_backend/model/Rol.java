package cl.cache.cache_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum Rol {
    ADMIN, VENDEDOR, CLIENTE;

    public String tipoUsuario(){
        return this == ADMIN ? "administrador" : name().toLowerCase();
    }

    @JsonCreator
    public static Rol desdeTexto(String texto){
        if (texto == null){
            return null;
        }
        return switch (texto.trim().toLowerCase()){
            case "administrador", "admin" -> ADMIN;
            case "vendedor" -> VENDEDOR;
            case "cliente" -> CLIENTE;
            default -> throw new IllegalArgumentException(("Tipo de usuario no valdo: "+ texto));
        };
    }
}