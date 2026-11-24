package com.mercado.altiplano.service;

import com.mercado.altiplano.dto.AuthDtos.LoginResponse;
import com.mercado.altiplano.exception.ApiException;
import com.mercado.altiplano.exception.AuthenticationException;
import com.mercado.altiplano.model.Usuario;
import com.mercado.altiplano.repository.UsuarioRepositoryPort;
import com.mercado.altiplano.security.PasswordHasher;
import com.mercado.altiplano.security.SesionService;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.Set;
import java.util.Locale;

@Service
public class AuthService {
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String MSG_INVALIDO = "Correo o contraseña incorrectos";
    private static final Set<String> CUENTAS_DEMO = Set.of("demo@kasero.local", "nilda@kasero.local", "admin@kasero.local");

    @Value("${kasero.demo.enabled:true}")
    private boolean demoEnabled = true;

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasher hasher;
    private final SesionService sesiones;

    public AuthService(UsuarioRepositoryPort usuarios, PasswordHasher hasher, SesionService sesiones) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.sesiones = sesiones;
    }

    public LoginResponse login(String correo, String password) {
        if (correo == null || password == null) throw new AuthenticationException(MSG_INVALIDO);
        if (!demoEnabled && CUENTAS_DEMO.contains(correo.trim().toLowerCase(Locale.ROOT)))
            throw new AuthenticationException(MSG_INVALIDO);
        Usuario u = usuarios.buscarPorCorreo(correo.trim())
                .filter(x -> hasher.verificar(password, x.passwordHash()))
                .orElseThrow(() -> new AuthenticationException(MSG_INVALIDO));
        return new LoginResponse(true, "Inicio de sesión exitoso", u.id(), u.nombre(), sesiones.crear(u.id()), u.rol());
    }

    public LoginResponse registrar(String nombre, String correo, String password) {
        if (nombre == null || nombre.isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe tu nombre");
        if (correo == null || !CORREO.matcher(correo.trim()).matches())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe un correo válido");
        if (password == null || password.length() < 8)
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener 8 o más caracteres");
        if (usuarios.buscarPorCorreo(correo.trim()).isPresent())
            throw new ApiException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        Usuario u = usuarios.guardar(nombre.trim(), correo.trim(), hasher.hash(password), "CLIENTE");
        return new LoginResponse(true, "Cuenta creada", u.id(), u.nombre(), sesiones.crear(u.id()), u.rol());
    }

    public Usuario actual(String authorization) {
        return usuarios.buscarPorId(sesiones.requerir(authorization))
                .orElseThrow(() -> new AuthenticationException("Inicia sesión para continuar"));
    }
}
