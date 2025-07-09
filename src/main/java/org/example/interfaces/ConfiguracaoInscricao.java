package org.example.interfaces;

import java.sql.SQLException;

import org.example.exceptions.ValorInvalidoException;

public interface ConfiguracaoInscricao {

    public double getValorInscricao(String role) throws SQLException;

    public void atualizarValorInscricao(String role, String valorInput) throws SQLException, ValorInvalidoException;

    public String listarValoresFormatado() throws SQLException;

}
