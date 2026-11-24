package com.mercado.altiplano.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mercado.altiplano.dto.AuthDtos.LoginResponse;
import com.mercado.altiplano.exception.ApiException;
import com.mercado.altiplano.exception.AuthenticationException;
import com.mercado.altiplano.model.Usuario;
import com.mercado.altiplano.repository.UsuarioRepositoryPort;
import com.mercado.altiplano.security.PasswordHasher;
import com.mercado.altiplano.security.SesionService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * R-07 Autenticación. Se prueba con un repositorio en memoria: gracias al puerto
 * UsuarioRepositoryPort (ADR-001) el servicio no depende de Supabase.
 */
class AuthServiceTest {

    /** Implementación de prueba del puerto. */
    static class UsuariosEnMemoria implements UsuarioRepositoryPort {
        private final Map<Integer, Usuario> datos = new HashMap<>();

        @Override
        public Optional<Usuario> buscarPorCorreo(String correo) {
            return datos.values().stream().filter(u -> u.correo().equalsIgnoreCase(correo)).findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(int id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public Usuario guardar(String nombre, String correo, String hash, String rol) {
            var u = new Usuario(datos.size() + 1, nombre, correo.toLowerCase(), hash, rol);
            datos.put(u.id(), u);
            return u;
        }
    }

    private AuthService auth;

    @BeforeEach
    void preparar() {
        auth = new AuthService(new UsuariosEnMemoria(), new PasswordHasher(), new SesionService());
    }

    private static int estado(Throwable t) {
        return ((ApiException) t).getStatus().value();
    }

    @Test
    void registraYPermiteIngresar() {
        auth.registrar("Ana Pérez", "ana@correo.com", "clave1234");
        LoginResponse r = auth.login("ana@correo.com", "clave1234");
        assertThat(r.exitoso()).isTrue();
        assertThat(r.nombre()).isEqualTo("Ana Pérez");
        assertThat(r.token()).isNotBlank();
    }

    @Test
    void elCorreoSeComparaSinImportarMayusculas() {
        auth.registrar("Ana", "Ana@Correo.com", "clave1234");
        assertThat(auth.login("ANA@CORREO.COM", "clave1234").exitoso()).isTrue();
    }

    @Test
    void elMensajeDeErrorNoRevelaSiFalloElCorreoOLaContrasena() {
        auth.registrar("Ana", "ana@correo.com", "clave1234");
        Throwable malaClave = org.assertj.core.api.Assertions.catchThrowable(() -> auth.login("ana@correo.com", "equivocada"));
        Throwable malCorreo = org.assertj.core.api.Assertions.catchThrowable(() -> auth.login("otro@correo.com", "clave1234"));
        assertThat(malaClave).isInstanceOf(AuthenticationException.class);
        assertThat(malCorreo).isInstanceOf(AuthenticationException.class);
        assertThat(malaClave.getMessage()).isEqualTo(malCorreo.getMessage());
    }

    @Test
    void elRegistroExigeNombre() {
        assertThatThrownBy(() -> auth.registrar("  ", "ana@correo.com", "clave1234"))
                .satisfies(e -> assertThat(estado(e)).isEqualTo(400));
    }

    @Test
    void elRegistroExigeUnCorreoValido() {
        assertThatThrownBy(() -> auth.registrar("Ana", "no-es-correo", "clave1234"))
                .satisfies(e -> assertThat(estado(e)).isEqualTo(400));
    }

    @Test
    void laContrasenaDebeTenerOchoCaracteresOMas() {
        assertThatThrownBy(() -> auth.registrar("Ana", "ana@correo.com", "corta"))
                .satisfies(e -> assertThat(estado(e)).isEqualTo(400));
        assertThat(auth.registrar("Ana", "ana@correo.com", "12345678").exitoso()).isTrue();
    }

    @Test
    void noSePermiteRegistrarDosVecesElMismoCorreo() {
        auth.registrar("Ana", "ana@correo.com", "clave1234");
        assertThatThrownBy(() -> auth.registrar("Otra Ana", "ANA@correo.com", "clave5678"))
                .satisfies(e -> assertThat(estado(e)).isEqualTo(HttpStatus.CONFLICT.value()));
    }

    @Test
    void ingresarSinDatosSeRechaza() {
        assertThatThrownBy(() -> auth.login(null, null)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void alDesactivarDemoSeBloqueanLasCuentasDemoExistentesPeroNoLosClientes() {
        var repo = new UsuariosEnMemoria();
        var hasher = new PasswordHasher();
        for (String correo : new String[]{"demo@kasero.local", "nilda@kasero.local", "admin@kasero.local"})
            repo.guardar("Cuenta demo", correo, hasher.hash("123456"), "ADMIN");
        var servicio = new AuthService(repo, hasher, new SesionService());
        org.springframework.test.util.ReflectionTestUtils.setField(servicio, "demoEnabled", false);
        for (String correo : new String[]{"demo@kasero.local", " Nilda@kasero.local ", "ADMIN@KASERO.LOCAL"})
            assertThatThrownBy(() -> servicio.login(correo, "123456")).isInstanceOf(AuthenticationException.class);
        servicio.registrar("Cliente", "cliente@correo.com", "clave1234");
        assertThat(servicio.login("cliente@correo.com", "clave1234").exitoso()).isTrue();
    }

    @Test
    void laContrasenaNoSeGuardaEnClaro() {
        var repo = new UsuariosEnMemoria();
        var servicio = new AuthService(repo, new PasswordHasher(), new SesionService());
        servicio.registrar("Ana", "ana@correo.com", "clave1234");
        assertThat(repo.buscarPorCorreo("ana@correo.com").orElseThrow().passwordHash()).doesNotContain("clave1234");
    }
}
