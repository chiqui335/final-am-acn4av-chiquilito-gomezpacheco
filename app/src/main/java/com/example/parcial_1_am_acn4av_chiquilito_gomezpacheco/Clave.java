package com.example.parcial_1_am_acn4av_chiquilito_gomezpacheco;

public class Clave {

    private String id;
    private String nombre;
    private String clave;
    private String fecha;
    private String tipo;

    public Clave() {}

    // 👇 ESTE ES EL QUE TE FALTA
    public Clave(String nombre, String clave, String fecha) {
        this.nombre = nombre;
        this.clave = clave;
        this.fecha = fecha;
    }

    public Clave(String nombre, String clave, String fecha, String tipo) {
        this.nombre = nombre;
        this.clave = clave;
        this.fecha = fecha;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}