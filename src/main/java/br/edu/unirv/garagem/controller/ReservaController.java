package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Reserva;
import br.edu.unirv.garagem.repository.IPessoaRepository;
import br.edu.unirv.garagem.repository.IReservaRepository;
import br.edu.unirv.garagem.repository.IVeiculoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ReservaController {
    private final IReservaRepository reservaRepository;
    private final IVeiculoRepository veiculoRepository;
    private final IPessoaRepository pessoaRepository;

    public ReservaController(IReservaRepository reservaRepository,
                             IVeiculoRepository veiculoRepository,
                             IPessoaRepository pessoaRepository) {
        this.reservaRepository = reservaRepository;
        this.veiculoRepository = veiculoRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/reservas";
    }

    @GetMapping("/reservas")
    public String index(@RequestParam(required = false) Integer editar, Model model) {
        Reserva reserva = editar == null ? new Reserva() : reservaRepository.obterPorId(editar).orElse(new Reserva());
        carregarTela(model, reserva);
        return "reserva/index";
    }

    @PostMapping("/reservas")
    public String adicionar(@ModelAttribute Reserva reserva, Model model, RedirectAttributes flash) {
        String erro = validarReserva(reserva, 0);
        if (erro != null) {
            model.addAttribute("erro", erro);
            carregarTela(model, reserva);
            return "reserva/index";
        }
        reservaRepository.adicionar(reserva);
        flash.addFlashAttribute("sucesso", "Reserva criada com sucesso.");
        return "redirect:/reservas";
    }

    @PostMapping("/reservas/{id}/editar")
    public String atualizar(@PathVariable int id, @ModelAttribute Reserva reserva,
                            Model model, RedirectAttributes flash) {
        if (reservaRepository.obterPorId(id).isEmpty()) {
            flash.addFlashAttribute("erro", "Reserva não encontrada.");
            return "redirect:/reservas";
        }
        reserva.setId(id);
        String erro = validarReserva(reserva, id);
        if (erro != null) {
            model.addAttribute("erro", erro);
            carregarTela(model, reserva);
            return "reserva/index";
        }
        reservaRepository.atualizar(reserva);
        flash.addFlashAttribute("sucesso", "Reserva atualizada com sucesso.");
        return "redirect:/reservas";
    }

    @PostMapping("/reservas/{id}/excluir")
    public String excluir(@PathVariable int id, RedirectAttributes flash) {
        if (reservaRepository.obterPorId(id).isEmpty()) {
            flash.addFlashAttribute("erro", "Reserva não encontrada.");
            return "redirect:/reservas";
        }
        reservaRepository.remover(id);
        flash.addFlashAttribute("sucesso", "Reserva cancelada com sucesso.");
        return "redirect:/reservas";
    }

    private String validarReserva(Reserva reserva, int idIgnorado) {
        if (reserva.getDataInicio() == null || reserva.getDataFim() == null) {
            return "Informe a data de início e a data de fim.";
        }
        if (reserva.getDataFim().isBefore(reserva.getDataInicio())) {
            return "A data de fim não pode ser anterior à data de início.";
        }
        if (veiculoRepository.obterPorId(reserva.getVeiculoId()).isEmpty()) {
            return "O veículo selecionado não existe.";
        }
        if (pessoaRepository.obterPorId(reserva.getPessoaId()).isEmpty()) {
            return "A pessoa selecionada não existe.";
        }
        if (reservaRepository.existeConflito(reserva.getVeiculoId(), reserva.getDataInicio(), reserva.getDataFim(), idIgnorado)) {
            return "Não foi possível realizar a reserva: o veículo já está reservado nesse período.";
        }
        return null;
    }

    private void carregarTela(Model model, Reserva reserva) {
        var veiculos = veiculoRepository.obterTodos();
        var pessoas = pessoaRepository.obterTodas();
        var reservas = reservaRepository.obterTodas();
        Map<Integer, String> veiculoNomes = veiculos.stream().collect(Collectors.toMap(
                v -> v.getId(), v -> v.getPlaca() + " - " + v.getModelo()));
        Map<Integer, String> pessoaNomes = pessoas.stream().collect(Collectors.toMap(
                p -> p.getId(), p -> p.getNome()));
        LocalDate hoje = LocalDate.now();
        Map<Integer, String> status = veiculos.stream().collect(Collectors.toMap(
                v -> v.getId(),
                v -> reservas.stream().anyMatch(r -> r.getVeiculoId() == v.getId()
                        && !hoje.isBefore(r.getDataInicio()) && !hoje.isAfter(r.getDataFim()))
                        ? "Reservado" : "Disponível"));

        model.addAttribute("reserva", reserva);
        model.addAttribute("reservas", reservas);
        model.addAttribute("veiculos", veiculos);
        model.addAttribute("pessoas", pessoas);
        model.addAttribute("veiculoNomes", veiculoNomes);
        model.addAttribute("pessoaNomes", pessoaNomes);
        model.addAttribute("statusVeiculos", status);
    }
}
