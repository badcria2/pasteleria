package com.pasteleria.cordova.service;


import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioService usuarioService; // Necesario para gestionar el usuario asociado

    @Transactional
    public Cliente crearCliente(Usuario usuario, String direccion) {
        // Asegúrate de que el usuario ya esté persistido (o se persistirá en cascada si la relación lo permite)
        if (usuario.getId() == null) {
            usuario = usuarioService.registrarUsuario(usuario); // Asumiendo que manejará encriptación, etc.
        }

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setDireccion(direccion);
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> findByUsuario(Usuario usuario) {
        return clienteRepository.findByUsuario(usuario);
    }

    public Optional<Cliente> findById(Integer clienteId) {
        return clienteRepository.findById(clienteId);
    }

    public List<Cliente> findAllClientes() {
        return clienteRepository.findAll();
    }

    public Cliente updateCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteCliente(Integer clienteId) {
        clienteRepository.deleteById(clienteId);
    }

    // Otros métodos de negocio relacionados con clientes
}