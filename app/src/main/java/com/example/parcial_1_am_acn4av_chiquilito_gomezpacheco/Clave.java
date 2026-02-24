package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;
public class Clave {
    private String nombre; //
    private String clave;  //
    private String fecha;  //
    private String id;

    // 1. Constructor
    public Clave() {
    }

    // Constructor
    public Clave(String nombre, String clave, String fecha) {
        this.nombre = nombre;
        this.clave = clave;
        this.fecha = fecha;
    }

    // Métodos Getter

    public String getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }

    public String getClave() {
        return clave;
    }

    public String getFecha() {
        return fecha;
    }

    // Métodos Setter

    public void setId(String id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}