package cl.cache.cache_backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadorUsuarioTest {

    @Test
    void runValido_aceptaRunsCorrectos() {
        assertThat(ValidadorUsuario.runValido("123456785")).isTrue();
        assertThat(ValidadorUsuario.runValido("111111111")).isTrue();
        assertThat(ValidadorUsuario.runValido("98765433")).isTrue();
    }

    @Test
    void runValido_rechazaDigitoVerificadorIncorrectoOFormato() {
        assertThat(ValidadorUsuario.runValido("123456789")).isFalse();
        assertThat(ValidadorUsuario.runValido("12345678-5")).isFalse();
        assertThat(ValidadorUsuario.runValido(null)).isFalse();
    }

    @Test
    void calcularDv_devuelveKy0() {
        assertThat(ValidadorUsuario.calcularDv("10000013")).isEqualTo("K");
        assertThat(ValidadorUsuario.calcularDv("10000004")).isEqualTo("0");
    }

    @Test
    void normalizarRun_quitaPuntosGuionYPoneKMayuscula() {
        assertThat(ValidadorUsuario.normalizarRun("10.000.013-k")).isEqualTo("10000013K");
    }

    @Test
    void dominioPermitido_soloDuocYGmail() {
        assertThat(ValidadorUsuario.dominioPermitido("ana@duoc.cl")).isTrue();
        assertThat(ValidadorUsuario.dominioPermitido("profe@profesor.duoc.cl")).isTrue();
        assertThat(ValidadorUsuario.dominioPermitido("ana@gmail.com")).isTrue();
        assertThat(ValidadorUsuario.dominioPermitido("ana@hotmail.com")).isFalse();
    }
}