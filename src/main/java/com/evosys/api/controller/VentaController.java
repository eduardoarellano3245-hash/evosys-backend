package com.evosys.api.controller;

import com.evosys.api.model.Venta;
import com.evosys.api.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaController {

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public List<Venta> listar() {
        return ventaRepository.findAll();
    }

    @PostMapping
    public Venta guardar(@RequestBody Venta venta) {

        System.out.println("=== VENTA RECIBIDA ===");
        System.out.println("Cliente: " + venta.getCliente());
        System.out.println("Total: " + venta.getTotal());

        Venta guardada = ventaRepository.save(venta);

        System.out.println("=== VENTA GUARDADA ID: " + guardada.getId_venta());

        return guardada;
    }
}