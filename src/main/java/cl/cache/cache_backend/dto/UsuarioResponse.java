package cl.cache.cache_backend.dto;

import cl.cache.cache_backend.model.Usuario;

public record UsuarioResponse(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        String tipoUsuario,
        String telefono,
        String region,
        String comuna,
        String direccion,
        String departamento,
        String fechaNacimiento
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(), u.getRol().tipoUsuario(),
                u.getTelefono(), u.getRegion(), u.getComuna(), u.getDireccion(), u.getDepartamento(),
                u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : null);
    }
}
