package org.example.interfaces;

import java.sql.SQLException;

import org.example.models.Participante;
import org.example.models.Usuario;

import org.example.exceptions.EmailDuplicadoException;
import org.example.exceptions.EmailInvalidoException;
import org.example.exceptions.SenhaFracaException;

public interface InterfaceUsuario {

    public void createUser(Participante user) throws SQLException, EmailDuplicadoException,
            SenhaFracaException, EmailInvalidoException;

    public Usuario login(String email, String senha) throws SQLException;
}
