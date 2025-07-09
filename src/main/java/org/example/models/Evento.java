package org.example.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Evento {

    private int id;
    private String nome;
    private String descricao;
    private String dataInicio;
    private String dataFim;
    private int vagasTotal;
    private int vagasDisponivel;

    public Evento(String nome, String descricao, String dataInicio, String dataFim, int vagasTotal,
                  int vagasDisponivel) {
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.vagasTotal = vagasTotal;
        this.vagasDisponivel = vagasDisponivel;
    }

    public Evento(String nome, String descricao, String dataInicio, String dataFim, int id) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

}