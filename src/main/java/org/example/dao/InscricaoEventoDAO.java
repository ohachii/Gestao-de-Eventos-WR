package org.example.dao;

import org.example.exceptions.InscricaoNaoPermitidaException;
import org.example.exceptions.InscricaoPendenteException;
import org.example.exceptions.VagasEsgotadasException;
import org.example.interfaces.EventoInscricao;
import org.example.exceptions.UsuarioJaInscritoException;
import org.example.utils.ConnectionFactory;

import java.sql.*;

public class InscricaoEventoDAO implements EventoInscricao {

    @Override
    public void inscreverUsuario(int usuarioId, int eventoId)
            throws SQLException, VagasEsgotadasException, UsuarioJaInscritoException,
            InscricaoPendenteException, InscricaoNaoPermitidaException {

        EventoDAO eventoDao = new EventoDAO();

        if (!eventoDao.temVagasDisponiveis(eventoId)) {
            throw new VagasEsgotadasException("Não há vagas disponíveis para este evento!");
        }

        if (temInscricaoPendente(usuarioId, eventoId)) {
            throw new InscricaoPendenteException("Você já tem uma inscrição pendente para este evento!");
        }

        if (usuarioJaInscrito(usuarioId, eventoId)) {
            throw new UsuarioJaInscritoException("Você já está inscrito neste evento!");
        }

        if (usuarioFoiRecusado(usuarioId, eventoId)) {
            throw new InscricaoNaoPermitidaException("Você já teve sua inscrição recusada para este evento!");
        }

        String sqlInserir = "INSERT INTO evento_user (usuario_id, evento_id, status_pagamento) VALUES (?, ?, 'PENDENTE')";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlInserir)) {

            comando.setInt(1, usuarioId);
            comando.setInt(2, eventoId);
            comando.executeUpdate();
        }
    }

    @Override
    public void atualizarStatusInscricao(int inscricaoId, String novoStatus) throws SQLException {
        if (!novoStatus.equals("CONFIRMADO") && !novoStatus.equals("RECUSADO") && !novoStatus.equals("PENDENTE")) {
            throw new SQLException("Status de inscrição inválido");
        }

        String sqlAtualizarStatus = "UPDATE evento_user SET status_pagamento = ? WHERE id = ?";
        String sqlAtualizarVagas = "UPDATE Evento SET vagas_disponivel = vagas_disponivel - 1 WHERE id = " +
                "(SELECT evento_id FROM evento_user WHERE id = ?)";

        Connection conexao = null;

        try {
            conexao = ConnectionFactory.getConnection();
            conexao.setAutoCommit(false);

            try (PreparedStatement comandoStatus = conexao.prepareStatement(sqlAtualizarStatus)) {
                comandoStatus.setString(1, novoStatus);
                comandoStatus.setInt(2, inscricaoId);
                comandoStatus.executeUpdate();
            }

            if (novoStatus.equals("CONFIRMADO")) {
                try (PreparedStatement comandoVagas = conexao.prepareStatement(sqlAtualizarVagas)) {
                    comandoVagas.setInt(1, inscricaoId);
                    comandoVagas.executeUpdate();
                }
            }

            conexao.commit();

        } catch (SQLException e) {
            if (conexao != null) conexao.rollback();
            throw e;
        } finally {
            if (conexao != null) conexao.setAutoCommit(true);
        }
    }

    @Override
    public String listarInscricoesPendentes() throws SQLException {
        String sqlConsulta = """
            SELECT eu.id, u.nome AS nomeUsuario, u.email, e.nome AS nomeEvento, 
                   e.data_inicio, e.data_fim, u.role AS tipoUsuario
            FROM evento_user eu
            JOIN User u ON eu.usuario_id = u.id
            JOIN Evento e ON eu.evento_id = e.id
            WHERE eu.status_pagamento = 'PENDENTE'
            ORDER BY e.data_inicio, u.nome
        """;

        StringBuilder resultado = new StringBuilder();

        try (Connection conexao = ConnectionFactory.getConnection();
             Statement comando = conexao.createStatement();
             ResultSet resultadoConsulta = comando.executeQuery(sqlConsulta)) {

            if (!resultadoConsulta.isBeforeFirst()) {
                return "\nNão há inscrições pendentes de confirmação.";
            }

            resultado.append("\n--- INSCRIÇÕES PENDENTES ---\n");
            resultado.append(String.format("%-5s %-20s %-20s %-30s %-15s %-15s%n",
                    "ID", "Usuário", "Tipo", "Evento", "Data Início", "Data Fim"));
            resultado.append("--------------------------------------------------------------------------------------------------\n");

            while (resultadoConsulta.next()) {
                resultado.append(String.format("%-5d %-20s %-20s %-30s %-15s %-15s%n",
                        resultadoConsulta.getInt("id"),
                        resultadoConsulta.getString("nomeUsuario"),
                        resultadoConsulta.getString("tipoUsuario"),
                        resultadoConsulta.getString("nomeEvento"),
                        resultadoConsulta.getString("data_inicio"),
                        resultadoConsulta.getString("data_fim")));
            }
        }

        return resultado.toString();
    }

    @Override
    public boolean usuarioTemInscricaoPendente(int usuarioId, int eventoId) throws SQLException {
        return false;
    }

    @Override
    public boolean usuarioJaRecusado(int usuarioId, int eventoId) throws SQLException {
        return false;
    }

    @Override
    public boolean usuarioJaPedente(int usuarioId, int eventoId) throws SQLException {
        return false;
    }

    private boolean usuarioJaInscrito(int usuarioId, int eventoId) throws SQLException {
        return contarInscricoes(usuarioId, eventoId, new String[]{"PENDENTE", "CONFIRMADO"}) > 0;
    }

    public boolean temInscricaoPendente(int usuarioId, int eventoId) throws SQLException {
        return contarInscricoes(usuarioId, eventoId, new String[]{"PENDENTE"}) > 0;
    }

    public boolean usuarioFoiRecusado(int usuarioId, int eventoId) throws SQLException {
        return contarInscricoes(usuarioId, eventoId, new String[]{"RECUSADO"}) > 0;
    }

    private int contarInscricoes(int usuarioId, int eventoId, String[] status) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM evento_user WHERE usuario_id = ? AND evento_id = ? AND status_pagamento IN (");
        for (int i = 0; i < status.length; i++) {
            query.append("?");
            if (i < status.length - 1) query.append(", ");
        }
        query.append(")");

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(query.toString())) {

            comando.setInt(1, usuarioId);
            comando.setInt(2, eventoId);
            for (int i = 0; i < status.length; i++) {
                comando.setString(i + 3, status[i]);
            }

            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    @Override
    public void cancelarInscricao(int usuarioId, int eventoId) throws SQLException {
        String sqlDeletar = "DELETE FROM evento_user WHERE usuario_id = ? AND evento_id = ?";
        String sqlAtualizarVagas = "UPDATE Evento SET vagas_disponivel = vagas_disponivel + 1 WHERE id = ?";

        Connection conexao = null;

        try {
            conexao = ConnectionFactory.getConnection();
            conexao.setAutoCommit(false);

            try (PreparedStatement comandoDeletar = conexao.prepareStatement(sqlDeletar);
                 PreparedStatement comandoVagas = conexao.prepareStatement(sqlAtualizarVagas)) {

                comandoDeletar.setInt(1, usuarioId);
                comandoDeletar.setInt(2, eventoId);

                int linhasAfetadas = comandoDeletar.executeUpdate();

                if (linhasAfetadas == 0) {
                    throw new SQLException("Inscrição não encontrada para cancelar.");
                }

                comandoVagas.setInt(1, eventoId);
                comandoVagas.executeUpdate();
            }

            conexao.commit();

        } catch (SQLException e) {
            if (conexao != null) conexao.rollback();
            throw e;
        } finally {
            if (conexao != null) conexao.setAutoCommit(true);
        }
    }

    @Override
    public String listarEventosConfirmadosDoUsuario(int usuarioId) throws SQLException {
        String sqlConsulta = """
            SELECT e.id, e.nome, e.descricao, e.data_inicio, e.data_fim, 
                   e.vagas_total, e.vagas_disponivel, eu.data_inscricao
            FROM evento_user eu
            JOIN Evento e ON eu.evento_id = e.id
            WHERE eu.usuario_id = ? AND eu.status_pagamento = 'CONFIRMADO'
            ORDER BY e.data_inicio
        """;

        StringBuilder resultado = new StringBuilder();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlConsulta)) {

            comando.setInt(1, usuarioId);
            ResultSet resultadoConsulta = comando.executeQuery();

            if (!resultadoConsulta.isBeforeFirst()) {
                return "\nVocê não está inscrito em nenhum evento confirmado.";
            }

            resultado.append("\n--- SEUS EVENTOS CONFIRMADOS ---\n");
            resultado.append(String.format("%-5s %-30s %-15s %-15s %-10s %-20s%n",
                    "ID", "Evento", "Data Início", "Data Fim", "Vagas", "Data Inscrição"));
            resultado.append("------------------------------------------------------------------------------------\n");

            while (resultadoConsulta.next()) {
                resultado.append(String.format("%-5d %-30s %-15s %-15s %-10d %-20s%n",
                        resultadoConsulta.getInt("id"),
                        resultadoConsulta.getString("nome"),
                        resultadoConsulta.getString("data_inicio"),
                        resultadoConsulta.getString("data_fim"),
                        resultadoConsulta.getInt("vagas_total"),
                        resultadoConsulta.getString("data_inscricao")));
            }
        }

        return resultado.toString();
    }

    @Override
    public String listarTodasInscricoesDoUsuario(int usuarioId) throws SQLException {
        String sqlConsulta = """
            SELECT 
                eu.id AS idInscricao,
                e.nome AS nomeEvento,
                eu.status_pagamento AS statusPagamento,
                e.data_inicio,
                e.data_fim,
                eu.data_inscricao
            FROM evento_user eu
            JOIN Evento e ON eu.evento_id = e.id
            WHERE eu.usuario_id = ?
            ORDER BY 
                CASE eu.status_pagamento
                    WHEN 'CONFIRMADO' THEN 1
                    WHEN 'PENDENTE' THEN 2
                    WHEN 'RECUSADO' THEN 3
                    ELSE 4
                END,
                eu.data_inscricao DESC
        """;

        StringBuilder resultado = new StringBuilder();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlConsulta)) {

            comando.setInt(1, usuarioId);
            ResultSet resultadoConsulta = comando.executeQuery();

            if (!resultadoConsulta.isBeforeFirst()) {
                return "\nVocê não possui nenhuma inscrição em eventos.";
            }

            resultado.append("\nTODAS AS SUAS INSCRIÇÕES EM EVENTOS\n");
            resultado.append(String.format("%-8s %-30s %-12s %-15s %-15s %-20s%n",
                    "ID", "Evento", "Status", "Data Início", "Data Fim", "Data Inscrição"));
            resultado.append("----------------------------------------------------------------------------------------\n");

            while (resultadoConsulta.next()) {
                String statusFormatado = formatarStatus(resultadoConsulta.getString("statusPagamento"));
                resultado.append(String.format("%-8d %-30s %-12s %-15s %-15s %-20s%n",
                        resultadoConsulta.getInt("idInscricao"),
                        resultadoConsulta.getString("nomeEvento"),
                        statusFormatado,
                        resultadoConsulta.getString("data_inicio"),
                        resultadoConsulta.getString("data_fim"),
                        resultadoConsulta.getString("data_inscricao")));
            }
        }

        return resultado.toString();
    }

    private String formatarStatus(String status) {
        switch (status) {
            case "CONFIRMADO":
                return "CONFIRMADO";
            case "PENDENTE":
                return "PENDENTE";
            case "RECUSADO":
                return "RECUSADO";
            default:
                return status;
        }
    }
}