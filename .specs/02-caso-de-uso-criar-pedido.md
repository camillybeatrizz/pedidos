# Spec 02 — Caso de uso: Criar Pedido

## Contexto

O domínio de `Pedido` já existe (`.specs/01-dominio-pedido.md`, implementado em
`domain/pedido`). Esta spec adiciona a primeira fatia da camada `application`,
orquestrando o domínio já existente sem duplicar suas regras. Nenhum adapter
(web, persistência) é criado aqui — só a camada de aplicação e as ports que ela
declara.

## Tarefa

Criar, em `br.com.pedidos.apiPedidos.application.pedido`:

- **`CriarPedido`** — port de entrada (interface) que declara a operação de criar um
  pedido.
- **`CriarPedidoService`** — implementação de `CriarPedido`, o caso de uso em si.
- **`Pedidos`** — port de saída (interface) para salvar um pedido.

`CriarPedidoService` depende apenas de `Pedidos` (interface) e do domínio
(`Pedido`, `ItemPedido`) — nunca de uma implementação concreta de persistência.

## Regras

1. O caso de uso recebe um identificador de cliente e uma lista de itens.
2. Uma lista de itens vazia é recusada — nenhum pedido é criado nem salvo.
3. O pedido de domínio é construído reaproveitando `Pedido.novo()` e
   `adicionarItem(...)` já existentes — nenhuma regra de domínio (quantidade/preço
   positivos, etc.) é duplicada na camada de aplicação.
4. O pedido criado é persistido através da port de saída `Pedidos`.
5. O caso de uso devolve o pedido salvo — o valor que a port de saída retornou, não uma
   cópia própria construída à parte.
6. A camada `application/pedido` não importa Spring, JPA, HTTP nem qualquer classe de
   adapter concreto — só o domínio e suas próprias ports.
7. `CriarPedido` é uma interface pública (port de entrada), sem detalhes de tecnologia
   na assinatura.
8. `Pedidos` é uma interface pública (port de saída), sem detalhes de tecnologia
   (nada de `Repository` do Spring Data, nada de JPA) na assinatura.

## Definição de pronto

1. Cliente e itens
   - ✅ Chamar o caso de uso com um `clienteId` e uma lista não vazia de itens válidos
     cria e devolve um pedido contendo esses itens.

2. Lista de itens vazia
   - ❌ Chamar o caso de uso com lista de itens vazia é recusado (lança exceção); a
     port de saída `Pedidos` não é chamada nesse caso (nenhum pedido é salvo).

3. Reaproveitamento do domínio
   - ✅ O pedido criado nasce ABERTO e com o total corretamente derivado dos itens
     (comportamento herdado de `Pedido`/`ItemPedido`, não reimplementado aqui).
   - ❌ Um item inválido (ex.: quantidade zero) propaga o erro do domínio
     (`ItemInvalidoException`), sem ser mascarado pela camada de aplicação.

4. Persistência via port de saída
   - ✅ O pedido criado é passado para `Pedidos` antes de ser devolvido.
   - ✅ O caso de uso devolve exatamente o pedido retornado pela port de saída (permite
     verificar, num teste com fake em memória, que o retorno é consistente com o que
     foi "salvo").

5. Isolamento de tecnologia
   - ✅ Verificação estrutural: nenhum arquivo em `application/pedido` contém
     `org.springframework`, `jakarta.persistence` ou `jakarta.servlet` no código-fonte
     (mesmo estilo de verificação já usado em `DominioSemFrameworkTest`, agora cobrindo
     `application/pedido`).

## Ambiguidades a decidir

1. **Identificador de cliente.** `Pedido` (spec 01) não tem campo para cliente. Proposta
   por padrão: `CriarPedido` recebe um `clienteId` (`String`) apenas como parâmetro de
   entrada do caso de uso, sem gravá-lo no domínio nesta etapa — estender `Pedido` para
   guardar cliente ficaria para uma spec própria. Confirma esse padrão, ou já quer que
   `Pedido` seja estendido agora?
2. **Exceção para lista vazia.** Proposta: uma exceção nova de aplicação,
   `ItensObrigatoriosException`, em `application/pedido` (distinta de
   `ItemInvalidoException`, que é sobre um item individual inválido, não sobre a lista
   estar vazia). Confirma o nome?
3. **Assinatura da port de saída.** Proposta: `Pedido salvar(Pedido pedido)` — recebe e
   devolve o mesmo pedido de domínio, sem gerar novo identificador (o UUID já vem do
   domínio). Confirma?
4. **Assinatura da port de entrada.** Proposta: `Pedido criar(String clienteId,
   List<ItemPedido> itens)` no método da interface `CriarPedido`. Confirma o nome do
   método (`criar`) e os tipos dos parâmetros?

Nenhum arquivo Java foi criado ou alterado — apenas este arquivo de spec.
