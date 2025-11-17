package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Administrador;
import com.pasteleria.cordova.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
    Optional<Administrador> findByUsuario(Usuario usuario);
}