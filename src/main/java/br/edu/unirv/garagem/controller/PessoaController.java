package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.repository.IPessoaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: recebe a requisição, chama o repositório (pela interface) e escolhe a View.
 * Não sabe como nem onde os dados são salvos.
 */
@Controller
@RequestMapping("/pessoas")
public class PessoaController {

    private final IPessoaRepository repositorio;

    // Injeção de dependência: o container entrega a implementação concreta.
    public PessoaController(IPessoaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pessoas", repositorio.obterTodas());
        return "pessoa/index";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pessoa", new Pessoa());
        model.addAttribute("titulo", "Nova pessoa");
        return "pessoa/PessoaForm";
    }

    @PostMapping("/novo")
    public String cadastrar(@Valid @ModelAttribute("pessoa") Pessoa pessoa,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes flash) {
        pessoa.setId(0);
        validarCpfUnico(pessoa, resultado);
        if (resultado.hasErrors()) {
            model.addAttribute("titulo", "Nova pessoa");
            return "pessoa/PessoaForm";
        }
        repositorio.adicionar(pessoa);
        flash.addFlashAttribute("sucesso", "Pessoa cadastrada com sucesso.");
        return "redirect:/pessoas";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes flash) {
        return repositorio.obterPorId(id)
                .map(pessoa -> {
                    model.addAttribute("pessoa", pessoa);
                    model.addAttribute("titulo", "Editar pessoa");
                    return "pessoa/PessoaForm";
                })
                .orElseGet(() -> naoEncontrada(flash));
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable int id,
                            @Valid @ModelAttribute("pessoa") Pessoa pessoa,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes flash) {
        if (repositorio.obterPorId(id).isEmpty()) {
            return naoEncontrada(flash);
        }
        pessoa.setId(id);
        validarCpfUnico(pessoa, resultado);
        if (resultado.hasErrors()) {
            model.addAttribute("titulo", "Editar pessoa");
            return "pessoa/PessoaForm";
        }
        repositorio.atualizar(pessoa);
        flash.addFlashAttribute("sucesso", "Pessoa atualizada com sucesso.");
        return "redirect:/pessoas";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable int id, RedirectAttributes flash) {
        if (repositorio.obterPorId(id).isEmpty()) {
            return naoEncontrada(flash);
        }
        repositorio.remover(id);
        flash.addFlashAttribute("sucesso", "Pessoa excluída com sucesso.");
        return "redirect:/pessoas";
    }

    private void validarCpfUnico(Pessoa pessoa, BindingResult resultado) {
        boolean cpfPreenchido = pessoa.getCpf() != null && !pessoa.getCpf().isBlank();
        if (cpfPreenchido && !resultado.hasFieldErrors("cpf")
                && repositorio.existeCpf(pessoa.getCpf(), pessoa.getId())) {
            resultado.rejectValue("cpf", "cpf.duplicado", "Já existe uma pessoa cadastrada com este CPF.");
        }
    }

    private String naoEncontrada(RedirectAttributes flash) {
        flash.addFlashAttribute("erro", "Pessoa não encontrada.");
        return "redirect:/pessoas";
    }
}
