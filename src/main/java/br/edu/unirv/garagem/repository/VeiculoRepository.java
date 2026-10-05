package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Veiculo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class VeiculoRepository implements IVeiculoRepository {

    private final ObjectMapper objectMapper;
    private final File arquivo;

    public VeiculoRepository(ObjectMapper objectMapper, @Value("${garagem.data.veiculos:data/veiculos.json}") String caminho) {
        this.objectMapper = objectMapper;
        this.arquivo = new File(caminho);
    }

    @Override
    public List<Veiculo> obterTodos() {
        try {
            if (!arquivo.exists()) {
                arquivo.getParentFile().mkdirs();
                objectMapper.writeValue(arquivo, new ArrayList<Veiculo>());
            }

            return objectMapper.readValue(
                    arquivo,
                    new TypeReference<List<Veiculo>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler veículos.", e);
        }
    }

    @Override
    public Optional<Veiculo> obterPorId(int id) {
        return obterTodos()
                .stream()
                .filter(v -> v.getId() == id)
                .findFirst();
    }

    @Override
    public void adicionar(Veiculo veiculo) {
        List<Veiculo> veiculos = obterTodos();

        int novoId = veiculos.stream()
                .mapToInt(Veiculo::getId)
                .max()
                .orElse(0) + 1;

        veiculo.setId(novoId);

        veiculos.add(veiculo);

        salvar(veiculos);
    }

    @Override
    public void atualizar(Veiculo veiculo) {
        List<Veiculo> veiculos = obterTodos();

        for (int i = 0; i < veiculos.size(); i++) {
            if (veiculos.get(i).getId() == veiculo.getId()) {
                veiculos.set(i, veiculo);
                break;
            }
        }

        salvar(veiculos);
    }

    @Override
    public boolean existePlaca(String placa, int idIgnorado) {
        String procurada = placa == null ? "" : placa.replaceAll("\\s", "").toUpperCase();
        return obterTodos().stream()
                .filter(v -> v.getId() != idIgnorado)
                .anyMatch(v -> v.getPlaca() != null && v.getPlaca().replaceAll("\\s", "").toUpperCase().equals(procurada));
    }

    @Override
    public void remover(int id) {
        List<Veiculo> veiculos = obterTodos();

        veiculos.removeIf(v -> v.getId() == id);

        salvar(veiculos);
    }

    private void salvar(List<Veiculo> veiculos) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(arquivo, veiculos);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar veículos.", e);
        }
    }
}