# Spec 01 — Domínio de Pedido

## Contexto

O projeto ainda não tem nenhum código de domínio (ver `AGENTS.md`): só existe o
esqueleto gerado pelo Spring Initializr. Esta é a primeira fatia de domínio a ser
implementada, seguindo a arquitetura hexagonal e as restrições já definidas em
`AGENTS.md` (domínio sem Spring/JPA, dinheiro em `BigDecimal`, sem Lombok).

## Tarefa

Modelar o domínio inicial de pedido, composto por três conceitos:

- **Pedido**: agrega os itens comprados e controla seu próprio ciclo de vida
  (aberto → pago/cancelado).
- **ItemPedido**: uma linha de compra (produto, quantidade e preço unitário).
- **StatusPedido**: os estados possíveis de um pedido.

Nenhum código de infraestrutura (persistência, web) faz parte desta tarefa — apenas o
domínio puro.

## Regras

Descritas em linguagem de comportamento; a nota entre parênteses é a orientação de
implementação já definida por você.

1. **Quantidade e preço de um item são sempre positivos.** Um item com quantidade
   zero/negativa ou preço zero/negativo não pode existir.
2. **O total do pedido é derivado, nunca armazenado.** O total é sempre calculado a
   partir dos itens no momento em que é consultado (soma de quantidade × preço unitário
   de cada item).
3. **Todo valor monetário é representado com `BigDecimal`.** Nunca `double`/`float`.
4. **Um pedido novo nasce no estado ABERTO.** (Implementação: `Pedido.novo(...)` cria um
   rascunho ABERTO, sem itens, identificado por um UUID gerado internamente.)
5. **Adicionar um item a um pedido não modifica o pedido existente — produz um novo
   pedido** com o item incluído. (Implementação: `adicionarItem(...)` devolve uma nova
   instância; a instância original permanece intacta.)
6. **Um pedido pago ou cancelado não aceita novos itens.** Tentar adicionar item nesses
   estados é uma operação recusada (erro), não uma alteração silenciosa.
7. **A lista de itens de um pedido não pode ser alterada por fora do domínio.** Quem
   consulta os itens não consegue, através da referência obtida, inserir/remover itens
   do pedido. (Implementação: `record`s + cópia defensiva na entrada e na saída.)
8. **Nenhuma dependência de Spring ou JPA neste código.** Domínio puro, sem anotações
   de framework.
9. **Somente um pedido ABERTO pode transicionar para PAGO ou para CANCELADO.** Qualquer
   outra transição (ex.: pagar um pedido já cancelado, cancelar um pedido já pago, pagar
   um pedido já pago) é recusada.

### Exemplo de referência

Cenário usado como caso de exemplo (da aula): um pedido com um item de `CAFE-500`,
quantidade 2, preço unitário `18.90` — total esperado `37.80`.

## Definição de pronto

Cada regra acima vira pelo menos um caso de teste. Casos de erro estão incluídos
explicitamente — não bastam os caminhos felizes.

1. Quantidade/preço positivos
   - ✅ Criar item com quantidade 2 e preço `18.90` é aceito.
   - ❌ Criar item com quantidade 0 é recusado.
   - ❌ Criar item com quantidade negativa é recusado.
   - ❌ Criar item com preço `0.00` é recusado.
   - ❌ Criar item com preço negativo é recusado.

2. Total derivado
   - ✅ Pedido com um item `CAFE-500`, quantidade 2, preço `18.90` → total `37.80`
     (caso de referência da aula).
   - ✅ Pedido sem itens → total `0.00` (ou equivalente — ver ambiguidade #4 abaixo).
   - ✅ Pedido com múltiplos itens → total é a soma dos subtotais de cada item.
   - ✅ O total não é um campo settable/armazenado — não existe forma de "forçar" um
     total diferente do calculado a partir dos itens.

3. Dinheiro com `BigDecimal`
   - ✅ Preço unitário e total são sempre `BigDecimal` (verificação de tipo/assinatura,
     não de comportamento).

4. Pedido novo começa ABERTO
   - ✅ `Pedido.novo(...)` retorna um pedido com status ABERTO.
   - ✅ `Pedido.novo(...)` retorna um pedido sem itens.
   - ✅ `Pedido.novo(...)` retorna um pedido com um identificador UUID não nulo.
   - ✅ Duas chamadas a `Pedido.novo(...)` produzem UUIDs diferentes.

5. Adicionar item devolve novo pedido
   - ✅ `pedido.adicionarItem(...)` retorna uma instância diferente da original.
   - ✅ O pedido original, após a chamada, continua sem o novo item (imutabilidade).
   - ✅ O novo pedido contém o item adicionado, além dos itens que o original já tinha.

6. Pedido pago ou cancelado não aceita item
   - ❌ Adicionar item a um pedido PAGO é recusado.
   - ❌ Adicionar item a um pedido CANCELADO é recusado.
   - ✅ Adicionar item a um pedido ABERTO é aceito (contraste com os dois casos acima).

7. Listas não modificáveis por fora
   - ❌ Obter a lista de itens de um pedido e tentar adicionar/remover um elemento
     diretamente nela é recusado (lança exceção ou não afeta o pedido, dependendo do
     tipo de lista escolhido — ver ambiguidade #5).
   - ✅ Modificar a lista de itens passada na construção do pedido, depois de criado o
     pedido, não afeta o pedido já construído (cópia defensiva na entrada).

8. Nenhuma dependência de Spring/JPA
   - ✅ Verificação estrutural: nenhuma classe do pacote de domínio importa
     `org.springframework.*` ou `jakarta.persistence.*`.

9. Transições de estado
   - ✅ Pagar um pedido ABERTO resulta em um pedido PAGO.
   - ✅ Cancelar um pedido ABERTO resulta em um pedido CANCELADO.
   - ❌ Pagar um pedido já PAGO é recusado.
   - ❌ Pagar um pedido CANCELADO é recusado.
   - ❌ Cancelar um pedido já CANCELADO é recusado.
   - ❌ Cancelar um pedido já PAGO é recusado.

## Ambiguidades a decidir

Antes de implementar, preciso que você resolva os pontos abaixo — a spec não define
isso sozinha:

1. **Identidade do cliente.** O exemplo da aula fala em "c-1 compra CAFE-500", mas a
   lista de conceitos do domínio inicial é só Pedido/ItemPedido/StatusPedido, sem
   Cliente. `c-1` deve virar um campo (ex.: `clienteId`) em `Pedido` nesta spec, ou é
   apenas contexto narrativo do exemplo e fica de fora do domínio por enquanto?
2. **Identificador do produto.** `ItemPedido` precisa de um código de produto (ex.:
   `CAFE-500`, aparentemente um SKU/string). Confirma que é uma `String` simples, sem
   um objeto/agregado `Produto` nesta etapa?
3. **Itens duplicados.** Se `adicionarItem` for chamado duas vezes com o mesmo código de
   produto (e mesmo preço), o resultado deve ser duas linhas separadas, ou as
   quantidades devem ser somadas em uma única linha? A spec atual não define isso.
4. **Total de pedido vazio e escala do `BigDecimal`.** Total de um pedido sem itens é
   `BigDecimal.ZERO`, `0.00` (escala 2) ou outro valor? E, de forma geral, o total e os
   preços devem ter escala fixa (2 casas decimais) ou preservam a escala como vier?
5. **Sinal de erro nas operações recusadas.** "É recusado" deve significar lançar uma
   exceção de domínio (ex.: `PedidoInvalidoException`/`TransicaoInvalidaException`), ou
   algum outro mecanismo (ex.: `Optional`/result type)? A spec assume "recusado = erro
   lançado", mas o tipo/nome da exceção não está definido.

Nenhum arquivo Java ou `pom.xml` foi alterado — apenas este arquivo de spec foi criado.
