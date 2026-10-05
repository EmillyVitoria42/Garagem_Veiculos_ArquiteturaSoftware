package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Pessoa;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Implementação em JSON. É a única classe que lê ou grava pessoas.json.
 * A cada operação de escrita, lê o arquivo, altera a lista e grava o arquivo inteiro de novo.
 */
@Repository
public class PessoaRepository implements IPessoaRepository {

    private static final TypeReference<List<Pessoa>> TIPO_LISTA = new TypeReference<>() {
    };

    private final Path arquivo;
    private final ObjectMapper mapper;

    public PessoaRepository(@Value("${garagem.data.pessoas:data/pessoas.json}") String caminho,
                            ObjectMapper mapper) {
        this.arquivo = Path.of(caminho);
        this.mapper = mapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        garantirArquivo();
    }

    @Override
    public synchronized List<Pessoa> obterTodas() {
        List<Pessoa> pessoas = ler();
        pessoas.sort(Comparator.comparingInt(Pessoa::getId));
        return pessoas;
    }

    @Override
    public synchronized Optional<Pessoa> obterPorId(int id) {
        return ler().stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public synchronized void adicionar(Pessoa pessoa) {
        List<Pessoa> pessoas = ler();
        int proximoId = pessoas.stream().mapToInt(Pessoa::getId).max().orElse(0) + 1;
        pessoa.setId(proximoId);
        pessoas.add(pessoa);
        gravar(pessoas);
    }

    @Override
    public synchronized void atualizar(Pessoa pessoa) {
        List<Pessoa> pessoas = ler();
        for (int i = 0; i < pessoas.size(); i++) {
            if (pessoas.get(i).getId() == pessoa.getId()) {
                pessoas.set(i, pessoa);
                gravar(pessoas);
                return;
            }
        }
    }

    @Override
    public synchronized void remover(int id) {
        List<Pessoa> pessoas = ler();
        if (pessoas.removeIf(p -> p.getId() == id)) {
            gravar(pessoas);
        }
    }

    @Override
    public synchronized boolean existeCpf(String cpf, int idIgnorado) {
        String procurado = somenteDigitos(cpf);
        return ler().stream()
                .filter(p -> p.getId() != idIgnorado)
                .anyMatch(p -> somenteDigitos(p.getCpf()).equals(procurado));
    }

    // ---- acesso ao arquivo (privado: nenhuma outra classe toca no JSON) ----

    private void garantirArquivo() {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            if (Files.notExists(arquivo)) {
                Files.writeString(arquivo, "[]");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar " + arquivo, e);
        }
    }

    private List<Pessoa> ler() {
        try {
            if (Files.notExists(arquivo) || Files.size(arquivo) == 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(mapper.readValue(arquivo.toFile(), TIPO_LISTA));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + arquivo, e);
        }
    }

    private void gravar(List<Pessoa> pessoas) {
        try {
            mapper.writeValue(arquivo.toFile(), pessoas);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar " + arquivo, e);
        }
    }

    private static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }
}
