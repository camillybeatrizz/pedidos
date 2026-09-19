# Spec 04 — Caso de uso: Adicionar Item

## Contexto

O domínio (`.specs/01-dominio-pedido.md`), o caso de uso `CriarPedido`
(`.specs/02-caso-de-uso-criar-pedido.md`) e os adapters REST/JPA
(`.specs/03-adapters-rest-e-jpa.md`) já existem e funcionam de ponta a ponta. Esta
spec adiciona o segundo caso de uso de ponta a ponta: adicionar um item a um pedido
já existente, reaproveitando o domínio (`Pedido.adicionarItem`), a port `Pedidos`
(spec 02, já tem `buscarPorId` e `salvar` — nenhum método novo é necessário) e o
`PedidoExceptionHandler` já existente (spec 03, apenas ampliado).

`CriarPedido`/`CriarPedidoService` **não são alterados** nesta spec — são um caso de
uso independente.

## Tarefa

Em `br.com.pedidos.apiPedidos.application.pedido`:

- `AdicionarItem` — port de entrada (interface), nova.
- `AdicionarItemService` — implementação do caso de uso, nova.
- `PedidoNaoEncontradoException` — nova exceção de aplicação, para quando o
  `Pedidos.buscarPorId` não encontra o pedido.

Em `br.com.pedidos.apiPedidos.adapters.entrada.rest`:

- Um novo método em `PedidoController` (mesma classe, mesmo recurso `/pedidos`):
  `POST /pedidos/{id}/itens`.
- `PedidoExceptionHandler` **ampliado** (mesma classe) com duas novas traduções:
  `PedidoNaoEncontradoException` → `404` e `PedidoFechadoException` (já existe no
  domínio, spec 01) → `409`.

Em `br.com.pedidos.apiPedidos.config`:

- `CasosDeUsoConfig` ganha um novo `@Bean` para `AdicionarItem`, reaproveitando o
  mesmo bean `Pedidos` já existente.

## Regras

1. `AdicionarItem` busca o pedido pelo `Pedidos.buscarPorId(id)` — não recebe o
   pedido pronto de fora.
2. Pedido inexistente (`buscarPorId` vazio) → `PedidoNaoEncontradoException`
   (aplicação). Nada é salvo.
3. Pedido encontrado, mas não `ABERTO` (`PAGO`/`CANCELADO`) → o próprio domínio já
   recusa (`Pedido.adicionarItem` lança `PedidoFechadoException`, spec 01); o caso de
   uso apenas propaga, sem duplicar a checagem de status. Nada é salvo.
4. Quantidade/preço inválidos → o próprio domínio já recusa na construção do
   `ItemPedido` (`ItemInvalidoException`, spec 01); o caso de uso não duplica essa
   validação. Nada é salvo.
5. Item válido em pedido `ABERTO`: `Pedido.adicionarItem` devolve um novo `Pedido`
   com o item incluído; o total é sempre derivado (nenhum código novo de soma é
   necessário — `Pedido.total()` já cobre isso, spec 01).
6. O pedido atualizado é persistido via `Pedidos.salvar(...)` — a mesma port já usada
   por `CriarPedido`.
7. `POST /pedidos/{id}/itens` devolve `200 OK` (não `201` — não cria um recurso novo,
   atualiza um existente) com a representação do pedido atualizado.
8. Pedido inexistente, na HTTP: `404`.
9. `PedidoExceptionHandler` é ampliado; nenhum `@ExceptionHandler` já existente
   (`ItemInvalidoException` → `422`, `@Valid`/JSON malformado → `400`) é alterado.
10. `CriarPedido`/`CriarPedidoService` não são tocados nesta spec.
11. `domain/` e `application/` continuam sem nenhum import de Spring/JPA/HTTP.
12. Nenhum endpoint além de `POST /pedidos/{id}/itens` é criado nesta etapa.

## Caso verificável de referência

Pedido existente (criado como no exemplo da spec 03): `CAFE-500`, quantidade 2,
preço `18.90` → total `37.80`. Adicionar 1 novo item `CAFE-500`, quantidade 1, preço
`18.90` → total `37.80 + 1 × 18.90 = 56.70`, **mesmo UUID** do pedido original (não
cria um pedido novo — a linha existente ganha uma segunda linha de item, sem somar
quantidades na mesma linha, mesma regra já usada em `CriarPedido`, spec 02,
Ambiguidade 3).

## Limites de arquivos

Podem ser criados/alterados nesta spec:
- Novo: `application/pedido/AdicionarItem.java`, `AdicionarItemService.java`,
  `PedidoNaoEncontradoException.java`.
- Alterado: `adapters/entrada/rest/PedidoController.java` (novo método),
  `adapters/entrada/rest/PedidoExceptionHandler.java` (dois novos handlers),
  `config/CasosDeUsoConfig.java` (novo bean).
- Testes correspondentes em `application/pedido/` e `adapters/entrada/rest/`.

Não podem ser alterados nesta spec:
- Nada em `domain/` (regras já existem: `PedidoFechadoException`,
  `ItemInvalidoException`, `Pedido.adicionarItem`, `Pedido.total()`).
- `application/pedido/CriarPedido.java`, `CriarPedidoService.java`,
  `ItensObrigatoriosException.java`, `Pedidos.java` (a interface já tem tudo que é
  necessário: `buscarPorId` e `salvar`).
- Nada em `infrastructure/pedido/jpa/` (o adapter `PedidosJpaAdapter` já implementa
  `Pedidos` por completo; nenhuma mudança de persistência é necessária).
- `pom.xml` (nenhuma dependência nova).

## Casos de teste (Definição de pronto)

Camada de aplicação (`AdicionarItemServiceTest`, com fake em memória de `Pedidos`,
mesmo estilo de `CriarPedidoServiceTest`):
1. ✅ Pedido existente, item válido → retorna pedido com o novo item na lista e total
   recalculado (caso de referência: `56.70`, mesmo UUID).
2. ✅ O pedido devolvido é o que `Pedidos.salvar` retornou (mesma disciplina de
   `CriarPedidoService`).
3. ❌ Pedido inexistente → `PedidoNaoEncontradoException`; `Pedidos.salvar` nunca é
   chamado.
4. ❌ Pedido `PAGO` → `PedidoFechadoException` propagada (não capturada/traduzida
   pelo caso de uso); `salvar` nunca é chamado.
5. ❌ Pedido `CANCELADO` → mesmo comportamento do caso 4.
6. ❌ Quantidade zero/negativa ou preço zero/negativo → `ItemInvalidoException`
   propagada (levantada na construção do `ItemPedido`, antes mesmo de chamar o caso
   de uso); `salvar` nunca é chamado.

Camada HTTP (ampliar `PedidoControllerTest`, MockMvc *standalone*, sem banco):
7. ✅ `POST /pedidos/{id}/itens` com item válido → `200`, corpo com total atualizado.
8. ❌ Pedido inexistente → `404`.
9. ❌ Pedido fechado (`PedidoFechadoException` do mock do caso de uso) → `409`.
10. ❌ Quantidade inválida (`ItemInvalidoException` do mock do caso de uso) → `422`
    (reaproveita o handler já existente — teste apenas confirma que continua
    funcionando com a nova rota).
11. ❌ Corpo malformado/campo ausente no novo endpoint → `400` (reaproveita o handler
    já existente).

Cenário manual (Postgres real, igual ao roteiro da spec 03): criar um pedido via
`POST /pedidos`, guardar o UUID, chamar `POST /pedidos/{id}/itens` com o item de
referência, comprovar total `56.70` na resposta e na tabela, e comprovar que as
chamadas de erro (item inválido, pedido inexistente, pedido fechado) não alteram
nenhuma linha no banco.

## Ambiguidades a decidir

1. **`PedidoResponse` exige `clienteId`, mas `Pedido` não guarda cliente.** Ao
   responder `POST /pedidos/{id}/itens`, não há `clienteId` disponível (o domínio não
   armazena isso, decisão da spec 02). Proposta: adicionar uma sobrecarga
   `PedidoResponse.de(Pedido pedido)` (sem `clienteId`, que fica `null` no JSON) usada
   só por este novo endpoint; a sobrecarga existente `PedidoResponse.de(Pedido,
   String clienteId)` continua servindo `POST /pedidos`. Confirma, ou prefere outra
   forma de representar a ausência de `clienteId` aqui?
2. **Corpo da requisição do novo endpoint.** Proposta: reaproveitar
   `PedidoRequest.ItemRequest` (já tem `sku`/`quantidade`/`precoUnitario` com
   `@NotBlank`/`@NotNull`) como corpo de `POST /pedidos/{id}/itens`, em vez de criar
   um DTO novo idêntico. Confirma?
3. **`{id}` inválido (não é um UUID)** não está coberto nas regras acima. Proposta:
   deixar o comportamento padrão do Spring (erro de conversão de tipo no
   `@PathVariable`, que hoje resulta em `400` sem handler dedicado) — não criar
   tratamento específico nesta spec, para não ampliar escopo. Confirma?
4. **Mesmo método HTTP em duas classes?** Proposta: o novo endpoint fica no mesmo
   `PedidoController` (mesmo recurso `/pedidos`), não numa classe nova. Confirma?

Nenhum arquivo Java foi criado ou alterado — apenas este arquivo de spec. Aguardando
sua revisão antes de propor o plano de implementação.
