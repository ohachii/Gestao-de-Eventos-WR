package org.example.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Administrador extends Usuario {
    public Administrador(String nome, String email, String senha) {
        super(nome, email, senha);
    }
}