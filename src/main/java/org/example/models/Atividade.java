package org.example.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Atividade {

    private int id;
    private String nome;
    private String descricao;
    private String data_realizacao;
    private String hora_inicio;
    private String hora_fim;
    private int limite_inscritos;
    private int vagas_disponivel;
    private String tipo;

    public Atividade (String nome, String descricao, String data_realizacao, String hora_inicio, String hora_fim,
                      int limite_inscritos, int vagas_disponivel, String tipo) {
        this.nome = nome;
        this.descricao = descricao;
        this.data_realizacao = data_realizacao;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
        this.limite_inscritos = limite_inscritos;
        this.vagas_disponivel = vagas_disponivel;
        this.tipo = tipo;
    }

    public Atividade (int id, String nome, String descricao, String data_realizacao, String hora_inicio,
                      String hora_fim, int limite_inscritos, int vagas_disponivel, String tipo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.data_realizacao = data_realizacao;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
        this.limite_inscritos = limite_inscritos;
        this.vagas_disponivel = vagas_disponivel;
        this.tipo = tipo;
    }

}