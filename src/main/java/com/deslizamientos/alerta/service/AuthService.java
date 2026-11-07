package com.deslizamientos.alerta.service;

import com.deslizamientos.alerta.dto.LoginDto;
import com.deslizamientos.alerta.dto.TokenDto;
import com.deslizamientos.alerta.exception.CredencialesInvalidasException;
import com.deslizamientos.alerta.model.Usuario;
import com.deslizamientos.alerta.repository.UsuarioRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Capa de negocio: el login. Comprueba la contraseña contra el hash BCrypt y, si
 * coincide, emite un token JWT firmado con el nombre de usuario y el rol.
 * El mismo mensaje sirve para usuario inexistente y contraseña incorrecta, para
 * no revelar qué usuarios existen.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final JwtEncoder jwtEncoder;
    private final long expiracionMinutos;

    public AuthService(UsuarioRepository usuarios,
                       PasswordEncoder codificador,
                       JwtEncoder jwtEncoder,
                       @Value("${seguridad.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwtEncoder = jwtEncoder;
        this.expiracionMinutos = expiracionMinutos;
    }

    @Transactional(readOnly = true)
    public TokenDto iniciarSesion(LoginDto dto) {
        Usuario usuario = usuarios.findByUsernameAndActivoTrue(dto.username())
                .filter(u -> codificador.matches(dto.password(), u.getPasswordHash()))
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"));

        Instant ahora = Instant.now();
        Instant vence = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiresAt(vence)
                .build();
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), datos))
                .getTokenValue();

        return new TokenDto(token, "Bearer", vence, usuario.getRol());
    }
}
