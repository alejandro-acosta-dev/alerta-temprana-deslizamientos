package com.deslizamientos.alerta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deslizamientos.alerta.dto.LoginDto;
import com.deslizamientos.alerta.dto.TokenDto;
import com.deslizamientos.alerta.exception.CredencialesInvalidasException;
import com.deslizamientos.alerta.model.Rol;
import com.deslizamientos.alerta.model.Usuario;
import com.deslizamientos.alerta.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * Pruebas del login sin base de datos ni HTTP. El repositorio y el codificador de
 * tokens son dobles (Mockito); el BCrypt es el real, para comprobar que la
 * contraseña se compara contra el hash. La excepción de credenciales es la que el
 * manejador global convierte en 401.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String CLAVE = "Clave2026*";

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private JwtEncoder jwtEncoder;

    private final PasswordEncoder codificador = new BCryptPasswordEncoder();

    private AuthService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AuthService(usuarios, codificador, jwtEncoder, 60);
    }

    private Usuario administrador() {
        return new Usuario("admin", codificador.encode(CLAVE), Rol.ADMINISTRADOR);
    }

    @Test
    void con_credenciales_correctas_devuelve_un_token_con_el_rol() {
        when(usuarios.findByUsernameAndActivoTrue("admin")).thenReturn(Optional.of(administrador()));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("token-de-prueba").header("alg", "HS256").subject("admin").build());

        TokenDto respuesta = servicio.iniciarSesion(new LoginDto("admin", CLAVE));

        assertEquals("token-de-prueba", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
        assertEquals(Rol.ADMINISTRADOR, respuesta.rol());
    }

    @Test
    void con_contrasena_incorrecta_es_credenciales_invalidas_401() {
        when(usuarios.findByUsernameAndActivoTrue("admin")).thenReturn(Optional.of(administrador()));

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion(new LoginDto("admin", "otra-clave")));
        verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void con_un_usuario_que_no_existe_es_credenciales_invalidas_401() {
        when(usuarios.findByUsernameAndActivoTrue("nadie")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion(new LoginDto("nadie", CLAVE)));
        verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
    }
}
