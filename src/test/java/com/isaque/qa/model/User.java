package com.isaque.qa.model;

public class User {
    public String nome;
    public String email;
    public String password;
    public String administrador;

    public User() {}

    public User(String nome, String email, String password, String administrador) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.administrador = administrador;
    }
}
