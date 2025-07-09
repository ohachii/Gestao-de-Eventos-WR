package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;

import org.example.exceptions.EmailDuplicadoException;
import org.example.exceptions.EmailInvalidoException;
import org.example.exceptions.SenhaFracaException;
import org.example.interfaces.InterfaceUsuario;
import org.example.models.Administrador;
import org.example.models.Participante;
import org.example.models.Usuario;
import org.example.utils.ConnectionFactory;

public class UsuarioDAO implements InterfaceUsuario {

    @Override
    public void createUser(Participante participante)
            throws SQLException, EmailDuplicadoException, SenhaFracaException, EmailInvalidoException {

        String email = participante.getEmail();
        String senha = participante.getSenha();

        if (!email.matches("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$")) {
            throw new EmailInvalidoException("Formato de email inválido! Ex: leonanmedanota@gmail.com");
        }

        if (emailExiste(email)) {
            throw new EmailDuplicadoException("O email " + email + " já está cadastrado!");
        }

        if (!senha.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$")) {
            throw new SenhaFracaException("""
                A senha deve conter:
                - Mínimo 6 caracteres
                - Pelo menos 1 letra maiúscula
                - Pelo menos 1 letra minúscula
                - Pelo menos 1 número
                """);
        }

        String sqlInserir = "INSERT INTO User (nome, email, senha, role) VALUES (?, ?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlInserir, Statement.RETURN_GENERATED_KEYS)) {

            String senhaCriptografada = BCrypt.hashpw(senha, BCrypt.gensalt());

            comando.setString(1, participante.getNome());
            comando.setString(2, email);
            comando.setString(3, senhaCriptografada);
            comando.setString(4, participante.getRole());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    participante.setId(resultado.getInt(1));
                }
            }
        }
    }

    @Override
    public Usuario login(String email, String senha) throws SQLException {
        String sqlBuscarUsuario = "SELECT * FROM User WHERE email = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlBuscarUsuario)) {

            comando.setString(1, email);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    String senhaSalva = resultado.getString("senha");

                    if (BCrypt.checkpw(senha, senhaSalva)) {
                        String role = resultado.getString("role");

                        if ("ADMIN".equals(role)) {
                            Administrador admin = new Administrador(
                                    resultado.getString("nome"),
                                    resultado.getString("email"),
                                    senhaSalva
                            );
                            admin.setId(resultado.getInt("id"));
                            return admin;
                        } else {
                            Participante participante = new Participante(
                                    resultado.getString("nome"),
                                    resultado.getString("email"),
                                    senhaSalva,
                                    role
                            );
                            participante.setId(resultado.getInt("id"));
                            return participante;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean emailExiste(String email) throws SQLException {
        String sqlVerificaEmail = "SELECT COUNT(*) FROM User WHERE email = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlVerificaEmail)) {

            comando.setString(1, email);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    int totalEmails = resultado.getInt(1);
                    return totalEmails > 0;
                }
                return false;
            }
        }
    }
}
