package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Veiculo;
import br.edu.unirv.garagem.repository.IVeiculoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/veiculos")
public class VeiculoController {

    private final IVeiculoRepository veiculoRepository;

    public VeiculoController(IVeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("veiculos", veiculoRepository.obterTodos());
        return "veiculo/index";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        model.addAttribute("titulo", "Novo veículo");
        return "veiculo/VeiculoForm";
    }

    @PostMapping
    public String adicionar(@ModelAttribute Veiculo veiculo, Model model, RedirectAttributes flash) {
        String erro = validar(veiculo, 0);
        if (erro != null) {
            model.addAttribute("erro", erro);
            model.addAttribute("titulo", "Novo veículo");
            return "veiculo/VeiculoForm";
        }
        veiculoRepository.adicionar(veiculo);
        flash.addFlashAttribute("sucesso", "Veículo cadastrado com sucesso.");
        return "redirect:/veiculos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes flash) {
        return veiculoRepository.obterPorId(id)
                .map(veiculo -> {
                    model.addAttribute("veiculo", veiculo);
                    model.addAttribute("titulo", "Editar veículo");
                    return "veiculo/VeiculoForm";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("erro", "Veículo não encontrado.");
                    return "redirect:/veiculos";
                });
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable int id, @ModelAttribute Veiculo veiculo,
                            Model model, RedirectAttributes flash) {
        if (veiculoRepository.obterPorId(id).isEmpty()) {
            flash.addFlashAttribute("erro", "Veículo não encontrado.");
            return "redirect:/veiculos";
        }
        veiculo.setId(id);
        String erro = validar(veiculo, id);
        if (erro != null) {
            model.addAttribute("erro", erro);
            model.addAttribute("titulo", "Editar veículo");
            return "veiculo/VeiculoForm";
        }
        veiculoRepository.atualizar(veiculo);
        flash.addFlashAttribute("sucesso", "Veículo atualizado com sucesso.");
        return "redirect:/veiculos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable int id, RedirectAttributes flash) {
        if (veiculoRepository.obterPorId(id).isEmpty()) {
            flash.addFlashAttribute("erro", "Veículo não encontrado.");
            return "redirect:/veiculos";
        }
        veiculoRepository.remover(id);
        flash.addFlashAttribute("sucesso", "Veículo excluído com sucesso.");
        return "redirect:/veiculos";
    }

    private String validar(Veiculo veiculo, int idIgnorado) {
        if (veiculo.getPlaca() == null || veiculo.getPlaca().isBlank()) return "A placa é obrigatória.";
        if (veiculo.getMarca() == null || veiculo.getMarca().isBlank()) return "A marca é obrigatória.";
        if (veiculo.getModelo() == null || veiculo.getModelo().isBlank()) return "O modelo é obrigatório.";
        if (veiculo.getAno() <= 0) return "O ano é obrigatório.";
        if (veiculoRepository.existePlaca(veiculo.getPlaca(), idIgnorado)) return "Já existe um veículo cadastrado com esta placa.";
        return null;
    }
}
