package cl.cache.cache_backend.exception;

//Responde 409 (conflicto: se rompre regla del negocio)
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

