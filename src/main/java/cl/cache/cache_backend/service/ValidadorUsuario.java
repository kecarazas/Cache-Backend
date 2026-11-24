package cl.cache.cache_backend.service;

import java.util.List;

//Reglas del formato del run y correo
public final class ValidadorUsuario {

    public static final List<String> DOMINIOS_PERMITIDOS = List.of("duoc.cl", "profesor.duoc.cl", "gmail.com");

    private ValidadorUsuario() {
    }

    //elimina puntos, guiones, espacios y deja la letra en mayuscula
    public static String normalizarRun(String run) {
        return run == null ? null : run.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
    }

    //Valida que tenga 7/8 digitos + el verificador
    public static boolean runValido(String run) {
        if (run == null || !run.matches("^\\d{7,8}[\\dK]$")) {
            return false;
        }
        String cuerpo = run.substring(0, run.length() - 1);
        String dv = run.substring(run.length() - 1);
        return calcularDv(cuerpo).equals(dv);
    }

    static String calcularDv(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return "0";
        if (resto == 10) return "K";
        return String.valueOf(resto);
    }

    public static boolean dominioPermitido(String correo) {
        if (correo == null || !correo.contains("@")) {
            return false;
        }
        String dominio = correo.substring(correo.lastIndexOf('@') + 1).toLowerCase();
        return DOMINIOS_PERMITIDOS.contains(dominio);
    }
}
