package br.edu.unirv.garagem.model;

import java.time.LocalDate;

/** Model da reserva. Guarda apenas os dados da reserva. */
public class Reserva {
    private int id;
    private int veiculoId;
    private int pessoaId;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    public Reserva() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVeiculoId() { return veiculoId; }
    public void setVeiculoId(int veiculoId) { this.veiculoId = veiculoId; }
    public int getPessoaId() { return pessoaId; }
    public void setPessoaId(int pessoaId) { this.pessoaId = pessoaId; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
}
