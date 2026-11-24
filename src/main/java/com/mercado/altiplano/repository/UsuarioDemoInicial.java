package com.mercado.altiplano.repository;

import com.mercado.altiplano.security.PasswordHasher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Crea las cuentas de prueba (contraseña 123456) si no existen: demo@kasero.local (cliente),
 * nilda@kasero.local (comerciante del puesto de Nilda) y admin@kasero.local (administrador).
 * Son solo para desarrollo: en producción deben cambiarse o eliminarse.
 */
@Component
@ConditionalOnProperty(prefix = "kasero.demo", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UsuarioDemoInicial implements ApplicationRunner {
    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasher hasher;
    private final JdbcTemplate jdbc;

    public UsuarioDemoInicial(UsuarioRepositoryPort usuarios, PasswordHasher hasher, JdbcTemplate jdbc) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.buscarPorCorreo("demo@kasero.local").isEmpty()) {
            usuarios.guardar("Usuario Demo", "demo@kasero.local", hasher.hash("123456"), "CLIENTE");
        }
        if (usuarios.buscarPorCorreo("nilda@kasero.local").isEmpty()) {
            usuarios.guardar("Nilda (puesto 5)", "nilda@kasero.local", hasher.hash("123456"), "COMERCIANTE");
        }
        if (usuarios.buscarPorCorreo("admin@kasero.local").isEmpty()) {
            usuarios.guardar("Administrador", "admin@kasero.local", hasher.hash("123456"), "ADMIN");
        }
        jdbc.update("""
                UPDATE usuarios SET comerciante_id = (SELECT id FROM comerciantes WHERE nombre = 'Nilda')
                WHERE correo = 'nilda@kasero.local' AND comerciante_id IS NULL""");
    }
}
