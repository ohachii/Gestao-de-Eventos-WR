package org.example.interfaces;

import java.sql.SQLException;

import org.example.exceptions.InscricaoNaoPermitidaException;
import org.example.exceptions.InscricaoPendenteException;
import org.example.exceptions.UsuarioJaInscritoException;
import org.example.exceptions.VagasEsgotadasException;

public interface AtividadeInscricao {
    public void inscreverUsuario(int userID, int AtividadeID)
            throws SQLException, VagasEsgotadasException, UsuarioJaInscritoException, InscricaoPendenteException, InscricaoNaoPermitidaException;

    public String listarAtividadesInscritas(int userID) throws SQLException;
}
