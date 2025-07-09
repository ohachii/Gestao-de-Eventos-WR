package org.example.interfaces;

import java.sql.SQLException;

import org.example.exceptions.InscricaoNaoPermitidaException;
import org.example.exceptions.InscricaoPendenteException;
import org.example.exceptions.UsuarioJaInscritoException;
import org.example.exceptions.VagasEsgotadasException;

public interface EventoInscricao{

    public void inscreverUsuario(int usuarioId, int eventoId)
            throws SQLException, VagasEsgotadasException, UsuarioJaInscritoException, InscricaoPendenteException, InscricaoNaoPermitidaException;

    public void atualizarStatusInscricao(int inscricaoId, String status) throws SQLException;

    public String listarInscricoesPendentes() throws SQLException;

    public boolean usuarioTemInscricaoPendente(int usuarioId, int eventoId) throws SQLException;

    public boolean usuarioJaRecusado(int usuarioId, int eventoId) throws SQLException;

    public boolean usuarioJaPedente(int usuarioId, int eventoId) throws SQLException;

    public void cancelarInscricao(int usuarioId, int eventoId) throws SQLException;

    public String listarEventosConfirmadosDoUsuario(int usuarioId) throws SQLException;

    public String listarTodasInscricoesDoUsuario(int userID) throws SQLException;
}
