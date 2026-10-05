package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Veiculo;

import java.util.List;
import java.util.Optional;

public interface IVeiculoRepository {

    List<Veiculo> obterTodos();

    Optional<Veiculo> obterPorId(int id);

    void adicionar(Veiculo veiculo);

    void atualizar(Veiculo veiculo);

    void remover(int id);

    boolean existePlaca(String placa, int idIgnorado);
}