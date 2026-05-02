package com.evosys.api.controller;

import com.evosys.api.model.Usuario;
import com.evosys.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario datos) {

        Optional<Usuario> usuario = usuarioRepository
                .findByUsuarioAndPassword(
                        datos.getUsuario(),
                        datos.getPassword()
                );

        if (usuario.isPresent()) {
            return ResponseEntity.ok(usuario.get());
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Usuario o contraseña incorrectos");
    }

    // ===== CREAR USUARIO =====
    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario nuevoUsuario) {
        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok(guardado);
    }

    // ===== LISTAR USUARIOS =====
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // ===== ELIMINAR USUARIO =====
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            if (!usuarioRepository.existsById(id)) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            usuarioRepository.deleteById(id);

            return ResponseEntity.ok("Usuario eliminado correctamente");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario");
        }
    }
}
