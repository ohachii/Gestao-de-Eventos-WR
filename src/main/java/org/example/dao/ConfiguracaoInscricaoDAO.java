package org.example.dao;

import java.sql.*;
import org.example.exceptions.ValorInvalidoException;
import org.example.interfaces.ConfiguracaoInscricao;
import org.example.utils.ConnectionFactory;

public class ConfiguracaoInscricaoDAO implements ConfiguracaoInscricao {

    @Override
    public double getValorInscricao(String tipoParticipante) throws SQLException {
        String sql = "SELECT valor FROM config_inscricao WHERE role = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, tipoParticipante);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getDouble("valor");
                } else {
                    throw new SQLException("Tipo de participante não encontrado: " + tipoParticipante);
                }
            }
        }
    }

    @Override
    public void atualizarValorInscricao(String tipoParticipante, String valorTexto)
            throws SQLException, ValorInvalidoException {

        tipoParticipante = tipoParticipante.toUpperCase();

        if (!tipoParticipante.equals("ALUNO") &&
                !tipoParticipante.equals("PROFESSOR") &&
                !tipoParticipante.equals("PROFISSIONAL")) {

            throw new IllegalArgumentException("Tipo de participante inválido: " + tipoParticipante);
        }

        double novoValor;
        try {
            String valorFormatado = valorTexto.replace(",", ".");
            novoValor = Double.parseDouble(valorFormatado);

            if (novoValor <= 0) {
                throw new ValorInvalidoException("O valor deve ser maior que zero.");
            }

            novoValor = Math.round(novoValor * 100.0) / 100.0;

        } catch (NumberFormatException e) {
            throw new ValorInvalidoException(
                    "Valor inválido. Digite um número válido (ex: 50.00 ou 50,00)"
            );
        }

        String sql = """
            UPDATE config_inscricao 
            SET valor = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE role = ?
        """;

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setDouble(1, novoValor);
            comando.setString(2, tipoParticipante);

            int linhasAfetadas = comando.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Tipo de participante não encontrado: " + tipoParticipante);
            }
        }
    }

    @Override
    public String listarValoresFormatado() throws SQLException {
        String sql = "SELECT role, valor FROM config_inscricao ORDER BY valor DESC";

        StringBuilder textoFormatado = new StringBuilder();
        textoFormatado.append("\n--- VALORES DE INSCRIÇÃO ---\n");
        textoFormatado.append(String.format("%-15s %-15s%n", "TIPO", "VALOR"));
        textoFormatado.append("---------------------------\n");

        try (Connection conexao = ConnectionFactory.getConnection();
             Statement comando = conexao.createStatement();
             ResultSet resultado = comando.executeQuery(sql)) {

            while (resultado.next()) {
                String tipo = resultado.getString("role");
                double valor = resultado.getDouble("valor");

                textoFormatado.append(String.format(
                        "%-15s R$%-15.2f%n", tipo, valor
                ));
            }
        }

        return textoFormatado.toString();
    }
}
