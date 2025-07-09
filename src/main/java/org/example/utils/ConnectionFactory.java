package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    private static final String URL = "jdbc:sqlite:src/main/resources/pharus2_eventos.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC não encontrado", e);
        }
    }

    public static void criarTabelas() {
        String sqlUsuarios = """
        CREATE TABLE IF NOT EXISTS User (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            senha TEXT NOT NULL,
            role TEXT CHECK(role IN ('ADMIN', 'ALUNO', 'PROFESSOR', 'PROFISSIONAL')),
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        );
        """;

        String sqlEventos = """
        CREATE TABLE IF NOT EXISTS Evento (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            descricao TEXT,
            data_inicio TEXT NOT NULL,
            data_fim TEXT NOT NULL,
            vagas_total INTEGER,
            vagas_disponivel INTEGER
        );
        """;

        String sqlEvento_user = """
        CREATE TABLE IF NOT EXISTS evento_user (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            usuario_id INTEGER NOT NULL,
            evento_id INTEGER NOT NULL,
            data_inscricao DATETIME DEFAULT CURRENT_TIMESTAMP,
            status_pagamento TEXT DEFAULT 'PENDENTE',
            FOREIGN KEY (usuario_id) REFERENCES User(id),
            FOREIGN KEY (evento_id) REFERENCES Evento(id),
            UNIQUE (usuario_id, evento_id)
        );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlEventos);
            stmt.execute(sqlEvento_user);
            System.out.println("Tabelas criadas com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

}