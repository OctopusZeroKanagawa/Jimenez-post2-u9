package com.universidad.seguridad.service;

import com.universidad.seguridad.model.Usuario;
import com.universidad.seguridad.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    /**
     * Registro público — sin restricción de rol.
     */
    @Transactional
    public void registrar(Usuario usuario) {
        if (repo.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        usuario.setContrasenia(encoder.encode(usuario.getContrasenia()));
        usuario.setRol("ROLE_USER");
        repo.save(usuario);
    }

    // -------------------------------------------------------
    // Métodos con @PreAuthorize — expresiones SpEL distintas
    // -------------------------------------------------------

    /**
     * Expresión 1 — hasRole
     * Solo ADMIN puede listar todos los usuarios.
     * Un USER recibirá AccessDeniedException → página 403.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarTodos() {
        return repo.findAll();
    }

    /**
     * Expresión 2 — hasRole OR parámetro == authentication.name
     * ADMIN puede ver cualquier perfil.
     * USER solo puede ver su propio perfil.
     */
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public Optional<Usuario> buscarPorEmail(String email) {
        return repo.findByEmail(email);
    }

    /**
     * Expresión 3 — hasRole (protección de escalada de privilegios)
     * Solo ADMIN puede cambiar el rol de un usuario.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void cambiarRol(Long id, String nuevoRol) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setRol(nuevoRol);
    }

    /**
     * Expresión 4 — objeto.campo == authentication.name OR hasRole
     * Un usuario solo puede actualizar su propio nombre.
     * ADMIN puede actualizar cualquiera.
     */
    @PreAuthorize("#usuario.email == authentication.name or hasRole('ADMIN')")
    @Transactional
    public void actualizarNombre(Usuario usuario) {
        Usuario existente = repo.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        existente.setNombre(usuario.getNombre());
    }

    /**
     * Expresión 5 — hasAnyRole
     * Tanto ADMIN como USER pueden consultar el total de usuarios.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public long contarUsuarios() {
        return repo.count();
    }
}
