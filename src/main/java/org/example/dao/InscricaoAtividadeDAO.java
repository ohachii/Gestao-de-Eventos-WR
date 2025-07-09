package org.example.dao;

import org.example.exceptions.InscricaoNaoPermitidaException;
import org.example.exceptions.InscricaoPendenteException;
import org.example.exceptions.VagasEsgotadasException;
import org.example.exceptions.UsuarioJaInscritoException;
import org.example.interfaces.AtividadeInscricao;
import org.example.utils.ConnectionFactory;
import java.sql.*;

public class InscricaoAtividadeDAO implements AtividadeInscricao {


    public void inscreverUsuario(int idUsuario, int idAtividade)
            throws SQLException, VagasEsgotadasException, UsuarioJaInscritoException,
            InscricaoPendenteException, InscricaoNaoPermitidaException {

        AtividadeDAO atividadeDao = new AtividadeDAO();


        if (usuarioEstaInscrito(idUsuario, idAtividade)) {
            throw new UsuarioJaInscritoException("Você já está inscrito nesta atividade!");
        }

        String sqlInserirInscricao = "INSERT INTO atividade_user (usuario_id, atividade_id) VALUES (?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlInserirInscricao)) {

            comando.setInt(1, idUsuario);
            comando.setInt(2, idAtividade);
            comando.executeUpdate();
        }

        String sqlAtualizaVagas = "UPDATE atividade SET vagas_disponiveis = vagas_disponiveis - 1 WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlAtualizaVagas)) {

            comando.setInt(1, idAtividade);
            comando.executeUpdate();
        }
    }

    private boolean usuarioEstaInscrito(int idUsuario, int idAtividade) throws SQLException {
        String sqlConsulta = "SELECT COUNT(*) FROM atividade_user WHERE usuario_id = ? AND atividade_id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlConsulta)) {

            comando.setInt(1, idUsuario);
            comando.setInt(2, idAtividade);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    int quantidade = resultado.getInt(1);
                    return quantidade > 0;
                }
                return false;
            }
        }
    }

    @Override
    public String listarAtividadesInscritas(int idUsuario) throws SQLException {
        String sqlConsulta = """
            SELECT a.id, a.nome, a.descricao, a.data_realizacao, a.hora_inicio, a.hora_fim,
                   a.limite_inscritos, a.vagas_disponiveis, au.data_inscricao
            FROM atividade_user au
            JOIN atividade a ON au.atividade_id = a.id
            WHERE au.usuario_id = ?
            ORDER BY a.data_realizacao
        """;

        StringBuilder resultadoTexto = new StringBuilder();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sqlConsulta)) {

            comando.setInt(1, idUsuario);
            ResultSet resultados = comando.executeQuery();

            if (!resultados.isBeforeFirst()) {
                return "\nVocê não está inscrito em nenhuma atividade.";
            }

            resultadoTexto.append("\n--- ATIVIDADES INSCRITAS ---\n");
            resultadoTexto.append(String.format("%-5s %-30s %-15s %-15s %-10s %-20s %-10s %n",
                    "ID", "Atividade", "Data Realização", "Hora Início", "Hora Fim", "Limite Inscritos", "Data Inscrição"));
            resultadoTexto.append("--------------------------------------------------------------------------------------------------------------------\n");

            while (resultados.next()) {
                resultadoTexto.append(String.format("%-5d %-30s %-20s %-15s %-15s %-10d %-20s %n",
                        resultados.getInt("id"),
                        resultados.getString("nome"),
                        resultados.getString("data_realizacao"),
                        resultados.getString("hora_inicio"),
                        resultados.getString("hora_fim"),
                        resultados.getInt("limite_inscritos"),
                        resultados.getString("data_inscricao")));
            }
        }

        return resultadoTexto.toString();
    }
}
