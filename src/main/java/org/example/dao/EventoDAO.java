package org.example.dao;

import org.example.models.Evento;
import org.example.utils.ConnectionFactory;
import org.example.interfaces.InterfaceDoEvento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventoDAO implements InterfaceDoEvento {

    @Override
    public void criarEvento(Evento novoEvento) throws SQLException {
        String sql = """
            INSERT INTO Evento (nome, descricao, data_inicio, data_fim, vagas_total, vagas_disponivel) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setString(1, novoEvento.getNome());
            comando.setString(2, novoEvento.getDescricao());
            comando.setString(3, novoEvento.getDataInicio());
            comando.setString(4, novoEvento.getDataFim());
            comando.setInt(5, novoEvento.getVagasTotal());
            comando.setInt(6, novoEvento.getVagasDisponivel());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    novoEvento.setId(resultado.getInt(1));
                }
            }
        }
    }

    @Override
    public Evento buscarPorId(int idEvento) throws SQLException {
        String sql = "SELECT * FROM Evento WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idEvento);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    Evento eventoEncontrado = new Evento(
                            resultado.getString("nome"),
                            resultado.getString("descricao"),
                            resultado.getString("data_inicio"),
                            resultado.getString("data_fim"),
                            resultado.getInt("vagas_total"),
                            resultado.getInt("vagas_disponivel")
                    );
                    eventoEncontrado.setId(idEvento);
                    return eventoEncontrado;
                }
            }
        }

        return null;
    }

    @Override
    public List<Evento> listarTodos() throws SQLException {
        List<Evento> listaEventos = new ArrayList<>();
        String sql = "SELECT * FROM Evento";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                Evento evento = new Evento(
                        resultado.getString("nome"),
                        resultado.getString("descricao"),
                        resultado.getString("data_inicio"),
                        resultado.getString("data_fim"),
                        resultado.getInt("vagas_total"),
                        resultado.getInt("vagas_disponivel")
                );
                evento.setId(resultado.getInt("id"));
                listaEventos.add(evento);
            }
        }

        return listaEventos;
    }

    @Override
    public String listarEventosFormatados() throws SQLException {
        List<Evento> listaEventos = listarTodos();
        StringBuilder textoFormatado = new StringBuilder();

        if (listaEventos.isEmpty()) {
            textoFormatado.append("Nenhum evento cadastrado.");
        } else {
            textoFormatado.append("\n--- LISTA DE EVENTOS ---\n");
            textoFormatado.append(String.format("%-5s %-50s %-15s %-15s %-10s %-10s%n",
                    "ID", "Nome", "Data Início", "Data Fim", "Vagas", "Disp."));
            textoFormatado.append("---------------------------------------------------------------------------------------------------------\n");

            for (Evento evento : listaEventos) {
                textoFormatado.append(String.format("%-5d %-50s %-15s %-15s %-10d %-10d%n",
                        evento.getId(),
                        evento.getNome(),
                        evento.getDataInicio(),
                        evento.getDataFim(),
                        evento.getVagasTotal(),
                        evento.getVagasDisponivel()));
            }
        }

        return textoFormatado.toString();
    }

    @Override
    public void editarEvento(Evento eventoEditado) throws SQLException {
        String sql = """
            UPDATE Evento 
            SET nome = ?, descricao = ?, data_inicio = ?, data_fim = ?
            WHERE id = ?
        """;

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, eventoEditado.getNome());
            comando.setString(2, eventoEditado.getDescricao());
            comando.setString(3, eventoEditado.getDataInicio());
            comando.setString(4, eventoEditado.getDataFim());
            comando.setInt(5, eventoEditado.getId());

            comando.executeUpdate();
        }
    }

    @Override
    public void deletarEvento(int idEvento) throws SQLException {
        String sql = "DELETE FROM Evento WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idEvento);
            comando.executeUpdate();
        }
    }

    @Override
    public boolean temVagasDisponiveis(int idEvento) throws SQLException {
        String sql = "SELECT vagas_disponivel FROM Evento WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idEvento);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() && resultado.getInt("vagas_disponivel") > 0;
            }
        }
    }
}
