package com.deslizamientos.alerta.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la API (AC de seguridad, HU-02 y HU-03):
 *  - Sin sesiones: cada petición trae su token JWT (cabecera Authorization: Bearer).
 *  - El token se firma y se valida con el secreto JWT_SECRET (HS256).
 *  - Las contraseñas se guardan con BCrypt.
 *  - Reglas por rol sobre /api/alertas: ADMINISTRADOR y TECNICO consultan (GET);
 *    solo ADMINISTRADOR crea, modifica y borra.
 *  - Públicos: el login, /api/estado y /actuator/health.
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private final SecretKey clave;

    public SeguridadConfig(@Value("${seguridad.jwt.secreto}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres (HS256 exige una clave de 256 bits)");
        }
        this.clave = new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http, RespuestasDeSeguridad respuestas)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/api/auth/login", "/api/estado", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/alertas/**").hasAnyRole("ADMINISTRADOR", "TECNICO")
                        .requestMatchers("/api/alertas/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(token -> token
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeRoles()))
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas));
        return http.build();
    }

    /** Lee el rol del claim "rol" del token y lo convierte en ROLE_ADMINISTRADOR o ROLE_TECNICO. */
    private JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("rol");
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(autoridades);
        return convertidor;
    }

    @Bean
    public JwtDecoder decodificadorJwt() {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtEncoder codificadorJwt() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    @Bean
    public PasswordEncoder codificadorDeClaves() {
        return new BCryptPasswordEncoder();
    }
}
