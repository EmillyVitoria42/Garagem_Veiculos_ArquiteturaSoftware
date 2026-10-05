package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IReservaRepository {
    List<Reserva> obterTodas();
    Optional<Reserva> obterPorId(int id);
    void adicionar(Reserva reserva);
    void atualizar(Reserva reserva);
    void remover(int id);

    boolean existeConflito(int veiculoId, LocalDate inicio, LocalDate fim, int idIgnorado);
}
