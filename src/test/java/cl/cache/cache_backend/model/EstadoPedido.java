package cl.cache.cache_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum EstadoPedido {
    RECIBIDO, ENVIADO, ENTREGADO, ANULADO;

    @JsonCreator
    public static EstadoPedido desdeTexto(String texto){
        if(texto == null){
            return null;
        }
        try{
            return valueOf(texto.trim().toUpperCase());
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Estado no valido: "+ texto);
        }
    }
}
