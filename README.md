# Garagem de Veículos

Atividade prática de Arquitetura de Software (ESW430 · UniRV).
Sistema de cadastro de pessoas e veículos com reservas, usando **MVC + Repository + Injeção de Dependência** e princípios **SOLID**.

## Status

- [x] **Entrega 1:** módulo Pessoas (listar, cadastrar, editar, excluir)
- [x] **Entrega 2:** Veículos + Reservas

## Integrante

- _(preencha com o seu nome)_

## Tecnologias

- Java 17+ (testado com JDK 21)
- Spring Boot 3.3 (Spring Web + Thymeleaf + Bean Validation)
- Persistência em arquivo JSON (Jackson)

## Como executar

Pré-requisitos: JDK 17 ou superior e Maven 3.9+ (ou abra o projeto no IntelliJ/VS Code/Eclipse).

```bash
mvn spring-boot:run
```

Depois acesse <http://localhost:8080> (abre diretamente em `/reservas`).

Os dados ficam em `data/pessoas.json` (pasta a partir de onde a aplicação é iniciada). Se o arquivo não existir, ele é criado com `[]`.

## Arquitetura (Entrega 1)

```
View (Thymeleaf) → PessoaController → IPessoaRepository → PessoaRepository → data/pessoas.json
```

| Papel | Arquivo |
| --- | --- |
| Model | `model/Pessoa.java` |
| Contrato | `repository/IPessoaRepository.java` |
| Implementação (JSON) | `repository/PessoaRepository.java` |
| Controller | `controller/PessoaController.java` |
| View do formulário | `templates/pessoa/PessoaForm.html` |
| View da listagem | `templates/pessoa/index.html` |

- O `PessoaController` recebe `IPessoaRepository` pelo **construtor**; o Spring entrega o `PessoaRepository` (anotado com `@Repository`).
- Somente o `PessoaRepository` lê e grava o JSON.
- As validações de campos ficam no Model (Bean Validation). A unicidade do CPF é verificada pelo repositório (`existeCpf`), e o Controller só exibe o erro.

## Rotas

| Operação | Rota |
| --- | --- |
| Listar | `GET /pessoas` |
| Cadastrar | `GET` e `POST /pessoas/novo` |
| Editar | `GET` e `POST /pessoas/{id}/editar` |
| Excluir | `POST /pessoas/{id}/excluir` (com confirmação na tela) |

## Ferramentas de IA utilizadas

- Claude (Anthropic): geração inicial do código da Entrega 1. _(ajuste conforme o que você realmente usar)_

## Prints

Adicione na pasta `docs/`:

- `pessoas-listagem.png`
- `pessoas-formulario.png`


## Arquitetura (Entrega 2)

```
View (Thymeleaf) → ReservaController → IReservaRepository → ReservaRepository → data/reservas.json
                           ↓
                 IVeiculoRepository / IPessoaRepository
```

### Veículos
- `model/Veiculo.java`
- `repository/IVeiculoRepository.java`
- `repository/VeiculoRepository.java`
- `controller/VeiculoController.java`
- `templates/veiculo/VeiculoForm.html`
- `templates/veiculo/index.html`
- Persistência em `data/veiculos.json`

### Reservas
- `model/Reserva.java`
- `repository/IReservaRepository.java`
- `repository/ReservaRepository.java`
- `controller/ReservaController.java`
- `templates/reserva/index.html`
- Persistência em `data/reservas.json`

A página inicial é Reservas. A regra de conflito usa intervalos inclusivos: `novaInicio <= existenteFim` e `novaFim >= existenteInicio`. A verificação fica no repositório, não no Controller. O status do veículo é calculado para a data atual.

## Testes finais sugeridos
1. Abrir `/` e confirmar que a página inicial é Reservas.
2. Cadastrar/editar/excluir um veículo e verificar `data/veiculos.json`.
3. Criar uma reserva para um veículo e uma pessoa.
4. Tentar outra reserva do mesmo veículo com período sobreposto e confirmar que ela é bloqueada.
5. Criar uma reserva em período sem sobreposição e confirmar que ela é aceita.
6. Editar uma reserva sem gerar conflito com ela mesma.
7. Cancelar uma reserva e confirmar a alteração em `data/reservas.json`.
8. Fechar e iniciar novamente a aplicação para confirmar a persistência.

## Ferramentas de IA utilizadas
- ChatGPT (OpenAI): auxílio na implementação e revisão das Etapas 6–12 da Entrega 2.
