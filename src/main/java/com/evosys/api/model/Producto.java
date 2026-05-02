package com.evosys.api.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "productos")
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_producto;

    private String nombre;
    private Double precio;
    private Integer stock;

    @Column(name = "codigo_barras")
    private String codigo_barras;

    private String imagen;
}