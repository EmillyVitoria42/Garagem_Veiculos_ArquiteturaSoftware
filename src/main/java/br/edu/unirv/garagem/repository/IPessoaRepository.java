package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Pessoa;
import java.util.List;
import java.util.Optional;

/**
 * Contrato do repositório de pessoas. O Controller conversa somente com esta interface.
 */
public interface IPessoaRepository {

    List<Pessoa> obterTodas();

    Optional<Pessoa> obterPorId(int id);

    void adicionar(Pessoa pessoa);

    void atualizar(Pessoa pessoa);

    void remover(int id);

    /**
     * Informa se já existe outra pessoa com o mesmo CPF (ignora pontuação).
     *
     * @param idIgnorado id da própria pessoa ao editar (use 0 ao cadastrar)
     */
    boolean existeCpf(String cpf, int idIgnorado);
}
