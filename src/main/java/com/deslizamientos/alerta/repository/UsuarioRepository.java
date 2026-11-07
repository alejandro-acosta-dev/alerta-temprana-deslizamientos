package com.deslizamientos.alerta.repository;

import com.deslizamientos.alerta.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Capa de datos: busca usuarios para el login. Un usuario desactivado
 * (borrado lógico, ADR-003) no puede iniciar sesión.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameAndActivoTrue(String username);
}
