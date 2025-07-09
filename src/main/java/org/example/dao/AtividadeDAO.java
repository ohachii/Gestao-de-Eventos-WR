package org.example.dao;

import org.example.models.Atividade;
import org.example.utils.ConnectionFactory;
import java.sql.*;
import java.util.*;

public class AtividadeDAO {

    private Atividade mapearAtividade(ResultSet resultado) throws SQLException {
        return new Atividade(
                resultado.getInt("id"),
                resultado.getString("nome"),
                resultado.getString("descricao"),
                resultado.getString("data_realizacao"),
                resultado.getString("hora_inicio"),
                resultado.getString("hora_fim"),
                resultado.getInt("limite_inscritos"),
                resultado.getInt("vagas_disponiveis"),
                resultado.getString("tipo")
        );
    }

    public void criarAtividade(Atividade atividade, int idEvento) throws SQLException {
        String sql = """
            INSERT INTO Atividade (
                evento_id, nome, descricao, data_realizacao, hora_inicio,
                hora_fim, limite_inscritos, vagas_disponiveis, tipo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setInt(1, idEvento);
            comando.setString(2, atividade.getNome());
            comando.setString(3, atividade.getDescricao());
            comando.setString(4, atividade.getData_realizacao());
            comando.setString(5, atividade.getHora_inicio());
            comando.setString(6, atividade.getHora_fim());
            comando.setInt(7, atividade.getLimite_inscritos());
            comando.setInt(8, atividade.getLimite_inscritos());
            comando.setString(9, atividade.getTipo());
            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    atividade.setId(resultado.getInt(1));
                }
            }
        }
    }

    public Atividade buscarAtividadePorId(int idAtividade) throws SQLException {
        String sql = "SELECT * FROM Atividade WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idAtividade);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? mapearAtividade(resultado) : null;
            }
        }
    }

    public List<Atividade> listarAtividadesPorEvento(int idEvento) throws SQLException {
        List<Atividade> listaAtividades = new ArrayList<>();
        String sql = "SELECT * FROM Atividade WHERE evento_id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idEvento);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    listaAtividades.add(mapearAtividade(resultado));
                }
            }
        }
        return listaAtividades;
    }

    public void atualizarAtividade(Atividade atividade) throws SQLException {
        String sql = """
            UPDATE Atividade SET 
                nome = ?, descricao = ?, data_realizacao = ?, 
                hora_inicio = ?, hora_fim = ?, limite_inscritos = ?, 
                vagas_disponiveis = ?, tipo = ?
            WHERE id = ?
        """;

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, atividade.getNome());
            comando.setString(2, atividade.getDescricao());
            comando.setString(3, atividade.getData_realizacao());
            comando.setString(4, atividade.getHora_inicio());
            comando.setString(5, atividade.getHora_fim());
            comando.setInt(6, atividade.getLimite_inscritos());
            comando.setInt(7, atividade.getVagas_disponivel());
            comando.setString(8, atividade.getTipo());
            comando.setInt(9, atividade.getId());

            comando.executeUpdate();
        }
    }

    public void deletarAtividade(int idAtividade) throws SQLException {
        String sql = "DELETE FROM Atividade WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idAtividade);
            comando.executeUpdate();
        }
    }

    public boolean atividadeTemVagas(int idAtividade) throws SQLException {
        String sql = "SELECT vagas_disponiveis FROM Atividade WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idAtividade);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt("vagas_disponiveis") > 0;
            }
        }
    }

    public String listarAtividadesFormatado(int idEvento) throws SQLException {
        List<Atividade> atividades = listarAtividadesPorEvento(idEvento);
        StringBuilder textoFormatado = new StringBuilder();

        if (atividades.isEmpty()) {
            textoFormatado.append("Nenhuma atividade cadastrada para este evento.");
        } else {
            textoFormatado.append("\n--- LISTA DE ATIVIDADES ---\n");
            textoFormatado.append(String.format(
                    "%-5s %-30s %-15s %-10s %-10s %-10s %-10s %-15s%n",
                    "ID", "Nome", "Data", "Início", "Fim", "Limite", "Disp.", "Tipo"));
            textoFormatado.append("---------------------------------------------------------------------------------------------------------\n");

            for (Atividade a : atividades) {
                textoFormatado.append(String.format(
                        "%-5d %-30s %-15s %-10s %-10s %-10d %-10d %-15s%n",
                        a.getId(), a.getNome(), a.getData_realizacao(),
                        a.getHora_inicio(), a.getHora_fim(),
                        a.getLimite_inscritos(), a.getVagas_disponivel(), a.getTipo()));
            }
        }
        return textoFormatado.toString();
    }
}
