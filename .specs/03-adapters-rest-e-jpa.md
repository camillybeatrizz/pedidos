# Spec 03 — Adapters REST e JPA

## Contexto

O domínio (`.specs/01-dominio-pedido.md`) e o caso de uso `CriarPedido`
(`.specs/02-caso-de-uso-criar-pedido.md`) já existem, isolados de framework. Falta
provar que esse núcleo funciona de ponta a ponta com infraestrutura real. Esta spec
organiza esse trabalho em três provas, que também funcionam como checkpoints
independentes:

- **Prova 1 — banco disponível.** `infra/docker-compose.yml` sobe um Postgres 16 e
  responde a `pg_isready`/`SELECT 1`. Não faz parte desta implementação — foi
  desenhada em conversa anterior e segue **pendente de aprovação e execução** (ver
  Ambiguidade 2).
- **Prova 2 — persistência pelo adapter.** Um `PedidosJpaAdapter` grava e lê um
  `Pedido` de verdade no Postgres via JPA, sem nenhum endpoint HTTP envolvido. **É o
  escopo desta implementação (checkpoint 4B).**
- **Prova 3 — HTTP.** Um adapter de entrada expõe o caso de uso via REST.
  **Contrato definido nesta revisão da spec, implementação também no escopo desta
  etapa (checkpoint 4C), junto com o checkpoint 4B.**

## Tarefa (checkpoint 4B)

Em `br.com.pedidos.apiPedidos.infrastructure.pedido.jpa`:

- `PedidoJpaEntity` e `ItemJpaEntity` — entidades JPA.
- `PedidoSpringDataRepository` — repositório Spring Data, uso interno da
  infraestrutura (não é a port `Pedidos`).
- `PedidoMapper` — conversão manual `Pedido` (domínio) ↔ entidades JPA. Sem MapStruct.
- `PedidosJpaAdapter` — implementa a port de saída `Pedidos` (spec 02), usando
  `PedidoSpringDataRepository` e `PedidoMapper`.

Em `src/main/resources/application.yml` — configuração do Postgres local
(`spring.jpa.open-in-view: false` explícito).

Em teste:

- `PedidosJpaAdapterIT` — teste de integração contra Postgres real, execução
  explícita (não roda com `./mvnw test` padrão).
- Renomear o teste de contexto gerado pelo Spring Initializr
  (`ApiPedidosApplicationTests`) para a mesma convenção `*IT` — sem apagar nem
  desativar, só adequar à convenção de execução explícita.

`domain/` e `application/` continuam sem nenhum import de Spring/JPA. A única mudança
necessária em `application/` é descrita na Ambiguidade 5 abaixo (um método novo na
port `Pedidos`, sem nenhum import de framework).

## Tarefa (checkpoint 4C — HTTP)

Em `br.com.pedidos.apiPedidos.adapters.entrada.rest`:

- `PedidoRequest` — DTO de entrada: `clienteId` (String) e `itens` (lista de
  `{sku, quantidade, precoUnitario}`).
- `PedidoResponse` — DTO de saída: `id` (UUID), `clienteId`, `itens`
  (`sku`/`quantidade`/`precoUnitario` de cada linha), `status`, `total`.
- `PedidoController` — `POST /pedidos`. Constrói `List<ItemPedido>` a partir do
  `PedidoRequest`, chama `CriarPedido.criar(clienteId, itens)`, devolve
  `PedidoResponse` com `201 Created`. **Não conhece JPA** — só a port de entrada
  `CriarPedido` e tipos de domínio (`ItemPedido`, `Pedido`).
- `PedidoExceptionHandler` (`@RestControllerAdvice`) — traduz exceções em respostas
  HTTP (ver Regras 12–14).

Em `br.com.pedidos.apiPedidos.config`:

- `CasosDeUsoConfig` (`@Configuration`) — declara o bean de `CriarPedido`
  (`CriarPedidoService`) recebendo o bean de `Pedidos` (`PedidosJpaAdapter`, do
  checkpoint 4B). É o único lugar do projeto que conecta explicitamente a
  implementação do caso de uso à implementação do adapter de saída.

### Contrato HTTP

**`POST /pedidos`**

Requisição:
```json
{
  "clienteId": "c-1",
  "itens": [
    { "sku": "CAFE-500", "quantidade": 2, "precoUnitario": 18.90 }
  ]
}
```

Resposta `201 Created`:
```json
{
  "id": "«uuid gerado pelo domínio»",
  "clienteId": "c-1",
  "itens": [
    { "sku": "CAFE-500", "quantidade": 2, "precoUnitario": 18.90 }
  ],
  "status": "ABERTO",
  "total": 37.80
}
```

`precoUnitario` e `total` são números decimais no JSON (`18.90`, `37.80`), nunca
strings.

Respostas de erro (corpo mínimo, com `message`):
- `422 Unprocessable Entity` — item inválido (quantidade/preço ≤ 0) ou lista de itens
  vazia: violações de regra de negócio, não de formato.
- `400 Bad Request` — JSON malformado (não parseável) ou campo obrigatório ausente
  (`clienteId` em branco/nulo, `itens` nulo, item sem `sku`/`quantidade`/
  `precoUnitario`): violações de formato/presença, verificadas via `@Valid` nos DTOs,
  nunca reimplementando a regra de positivos aqui.

Importante: a lista `itens` vazia (`"itens": []`) **passa** pela validação de
formato/presença (`@Valid` não pode marcar isso como erro) e só é recusada mais
adiante, pela regra de negócio existente (`ItensObrigatoriosException`, ver
Ambiguidade 6) — por isso o resultado é `422`, não `400`.

## Regras

1. Somente `Data JPA` e `PostgreSQL Driver` podem ser considerados para o `pom.xml`
   nesta etapa — nenhuma outra dependência nova (sem H2, sem Lombok, sem MapStruct).
2. Tabela `pedido`: a chave primária é o UUID do domínio — a entidade não gera outro
   id (sem `@GeneratedValue` no id do pedido).
3. O total nunca é uma coluna. Ao reconstruir um `Pedido` a partir do banco, o total
   continua sendo calculado por `Pedido.total()`, a partir dos itens lidos.
4. Tabela de itens guarda `sku` (código do produto), `quantidade` e `precoUnitario` —
   nenhum outro campo de negócio.
5. `domain/` e `application/` não importam Spring nem JPA. Só `infrastructure/`
   conhece essas tecnologias.
6. O adapter mapeia os itens para objetos de domínio **dentro de uma transação**,
   antes de fechar a sessão — necessário porque `spring.jpa.open-in-view=false`.
7. `spring.jpa.open-in-view: false` é configurado explicitamente em
   `application.yml`.
8. Sem H2 (o teste de integração usa Postgres real), sem Lombok, sem MapStruct.
9. Os testes unitários já existentes (domínio + aplicação) continuam passando sem
   precisar de banco — rodam no `./mvnw test` normal.
10. Testes de integração usam o sufixo `*IT` e não rodam com `./mvnw test` padrão —
    exigem execução explícita. Isso vale também para o teste de contexto do Spring
    Boot, que é renomeado para essa convenção em vez de apagado ou desativado.
11. `PedidoController` depende só da port `CriarPedido` e de tipos de domínio — nunca
    de `PedidosJpaAdapter`, `PedidoJpaEntity` ou qualquer classe de
    `infrastructure/.../jpa`.
12. `ItemInvalidoException` e a exceção de "lista de itens vazia" (ver Ambiguidade 6)
    viram `422`, com uma mensagem legível no corpo.
13. Falha de `@Valid` (campo obrigatório ausente/nulo) e JSON malformado
    (`HttpMessageNotReadableException`) viram `400`.
14. A regra de quantidade/preço positivos nunca é reimplementada no DTO/controller —
    continua exclusivamente em `ItemPedido` (domínio). `@Valid` cobre só formato e
    presença.
15. Nenhum outro endpoint além de `POST /pedidos` é criado nesta etapa.

## Definição de pronto — as três provas

1. **Banco disponível** (fora desta implementação): `pg_isready` responde OK e
   `SELECT 1` executa contra o Postgres configurado. Depende da Ambiguidade 2.
2. **Persistência pelo adapter** (esta implementação):
   - ✅ Salvar, via `PedidosJpaAdapter`, um pedido de `c-1` com item `CAFE-500`,
     quantidade 2, preço `18.90`.
   - ✅ Recuperar esse mesmo pedido pelo UUID, numa transação nova (chamada separada,
     não dentro da mesma transação do salvar) — comprova que a leitura não depende de
     `open-in-view`.
   - ✅ O pedido recuperado tem status `ABERTO`, os itens corretos, e total `37.80`
     (calculado, não lido de coluna).
   - ✅ Consulta direta à tabela `pedido`/tabela de itens mostra a linha salva com o
     UUID do domínio (não um id gerado à parte) e os campos `sku`/`quantidade`/
     `precoUnitario` corretos.
3. **HTTP** (esta implementação):
   - ✅ `POST /pedidos` com `clienteId="c-1"`, item `CAFE-500`, quantidade 2, preço
     `18.90` → `201`, corpo com `total: 37.80`.
   - ❌ Mesmo corpo com `quantidade: 0` → `422`, com mensagem; nenhuma linha
     adicionada ao banco.
   - ❌ Corpo com `itens: []` → `422`, com mensagem; nenhuma linha adicionada.
   - ❌ Corpo com JSON malformado (sintaxe inválida) → `400`; nenhuma linha
     adicionada.
   - ✅ Consultar o pedido criado pelo UUID retornado, diretamente no banco, mostra a
     linha e seus itens.
   - ✅ Reiniciar a aplicação e consultar de novo pelo mesmo UUID mostra a mesma linha
     e os mesmos itens (prova de persistência real, não em memória).

## Ambiguidades a decidir

1. **`pom.xml` já tem Data JPA e PostgreSQL Driver.** Essas duas dependências já
   estavam presentes desde antes desta sessão (sinalizado no diagnóstico inicial).
   "Adicionar somente Data JPA e PostgreSQL Driver" já está satisfeito — não pretendo
   editar `pom.xml` nesta etapa. Confirma que não há necessidade de mexer nisso, ou
   você quer que eu revise as versões/escopos atuais primeiro?
2. **Não existe Postgres deste projeto rodando ainda.** O `infra/docker-compose.yml`
   (Postgres 16, db/user/senha `pedidos`) só foi planejado, nunca aprovado nem criado.
   O único processo na porta 5432 hoje é um Postgres nativo do Windows, sem relação
   com este projeto. Sem um Postgres deste projeto no ar, os testes `*IT` não têm
   como conectar. Preciso que você diga: retomamos e aprovamos agora a criação/subida
   do `infra/docker-compose.yml`, ou já existe outro Postgres local (host/porta/
   credenciais) que devo apontar no `application.yml`?
3. **Tipo da coluna do id do pedido.** Proposta: `UUID` nativo do Postgres
   (`id uuid primary key`), entidade JPA com campo `UUID`. Confirma, ou prefere
   `String`/`varchar`?
4. **Tabela e chave estrangeira dos itens.** Proposta: tabela `item_pedido`, com
   `pedido_id` como FK; a linha de item tem uma chave técnica própria
   (`@GeneratedValue`), interna à infraestrutura — nunca exposta ao domínio. Confirma?
5. **A port `Pedidos` (spec 02) só tem `salvar`.** Para a prova 2 ("recuperar por
   UUID"), a port de saída precisa ganhar um método de busca, ex.:
   `Optional<Pedido> buscarPorId(UUID id)`. Isso é uma pequena extensão de
   `application/pedido/Pedidos.java` (sem nenhum import de framework — só
   `java.util.UUID`/`Optional`/o próprio `Pedido`). Confirma que posso acrescentar
   esse método à interface já existente?
6. **Nome da exceção de "lista de itens vazia".** Já existe, implementada na spec 02,
   como `ItensObrigatoriosException` em `application/pedido`. Esta mensagem pediu o
   nome `PedidoSemItensException`. Proposta: manter `ItensObrigatoriosException` (já
   implementada e testada) e apontar o handler HTTP para ela, em vez de renomear uma
   classe já pronta. Confirma, ou prefere renomear para `PedidoSemItensException`?
7. **Pacote dos adapters de entrada.** Esta mensagem pede
   `adapters/entrada/rest`, mas `AGENTS.md` descreve a camada de tecnologia como
   `infrastructure` (que ainda não existe no projeto — o checkpoint 4B usaria
   `infrastructure.pedido.jpa`). Proposta: usar exatamente o pacote pedido agora
   (`br.com.pedidos.apiPedidos.adapters.entrada.rest`) para a parte HTTP, e manter
   `infrastructure.pedido.jpa` para a parte JPA — os dois são "adapters" na
   arquitetura hexagonal, só em pacotes-raiz de nomes diferentes por ora. Quer que eu
   unifique os dois sob um único nome de pacote-raiz (ex.: tudo em `adapters/...`),
   ou seguimos com essa divergência de nomenclatura por enquanto (e alinhamos depois,
   numa limpeza à parte)?
8. **Pré-requisito ainda pendente: banco do projeto.** Os checkpoints 4B e 4C desta
   spec dependem de um Postgres deste projeto no ar (Ambiguidade 2, acima) para os
   testes `*IT` e para os passos manuais de `POST /pedidos` + consulta + reinício da
   aplicação. Preciso da sua decisão sobre isso antes de rodar qualquer coisa contra
   banco de verdade (rodar os testes unitários sem banco não depende disso).

Nenhum arquivo Java foi criado ou alterado — apenas este arquivo de spec.
