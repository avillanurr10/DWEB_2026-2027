package es.mariana.dweb.exception;

/* Excepción que comunica errores de validación del servicio al controlador. */
public class EntrenadorException extends RuntimeException {
    public EntrenadorException(String mensaje) { super(mensaje); }
}
