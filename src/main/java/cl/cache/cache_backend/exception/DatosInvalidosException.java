package cl.cache.cache_backend.exception;

//Responde HTTP 400
public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
