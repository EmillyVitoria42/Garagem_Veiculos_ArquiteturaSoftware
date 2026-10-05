package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Reserva;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservaRepository implements IReservaRepository {
    private static final TypeReference<List<Reserva>> TIPO_LISTA = new TypeReference<>() {};

    private final Path arquivo;
    private final ObjectMapper mapper;

    public ReservaRepository(@Value("${garagem.data.reservas:data/reservas.json}") String caminho,
                             ObjectMapper objectMapper) {
        this.arquivo = Path.of(caminho);
        this.mapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        garantirArquivo();
    }

    @Override
    public synchronized List<Reserva> obterTodas() {
        List<Reserva> reservas = ler();
        reservas.sort(Comparator.comparing(Reserva::getDataInicio, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Reserva::getId));
        return reservas;
    }

    @Override
    public synchronized Optional<Reserva> obterPorId(int id) {
        return ler().stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public synchronized void adicionar(Reserva reserva) {
        List<Reserva> reservas = ler();
        int proximoId = reservas.stream().mapToInt(Reserva::getId).max().orElse(0) + 1;
        reserva.setId(proximoId);
        reservas.add(reserva);
        gravar(reservas);
    }

    @Override
    public synchronized void atualizar(Reserva reserva) {
        List<Reserva> reservas = ler();
        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId() == reserva.getId()) {
                reservas.set(i, reserva);
                gravar(reservas);
                return;
            }
        }
    }

    @Override
    public synchronized void remover(int id) {
        List<Reserva> reservas = ler();
        if (reservas.removeIf(r -> r.getId() == id)) {
            gravar(reservas);
        }
    }

    @Override
    public synchronized boolean existeConflito(int veiculoId, LocalDate inicio, LocalDate fim, int idIgnorado) {
        return ler().stream()
                .filter(r -> r.getId() != idIgnorado)
                .filter(r -> r.getVeiculoId() == veiculoId)
                .anyMatch(r -> !inicio.isAfter(r.getDataFim()) && !fim.isBefore(r.getDataInicio()));
    }

    private void garantirArquivo() {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) Files.createDirectories(pasta);
            if (Files.notExists(arquivo)) Files.writeString(arquivo, "[]");
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar " + arquivo, e);
        }
    }

    private List<Reserva> ler() {
        try {
            if (Files.notExists(arquivo) || Files.size(arquivo) == 0) return new ArrayList<>();
            return new ArrayList<>(mapper.readValue(arquivo.toFile(), TIPO_LISTA));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + arquivo, e);
        }
    }

    private void gravar(List<Reserva> reservas) {
        try {
            mapper.writeValue(arquivo.toFile(), reservas);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar " + arquivo, e);
        }
    }
}
