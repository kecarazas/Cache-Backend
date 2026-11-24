package cl.cache.cache_backend.exception;

//Responde HTTP 401
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}

