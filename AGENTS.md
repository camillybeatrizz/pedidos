# AGENTS.md — regras do projeto apiPedidos

Esta é a fonte principal de regras para qualquer agente (humano ou IA) que trabalhe neste
repositório. Outras instruções (ex.: `CLAUDE.md`) apontam para este arquivo e não devem
duplicá-lo.

## Objetivo do serviço

`apiPedidos` é um serviço backend para gestão de pedidos (criação, consulta e
acompanhamento do ciclo de vida de um pedido). Este objetivo é uma descrição inicial —
ajuste este parágrafo assim que o domínio de negócio for detalhado em uma spec.

## Stack

- Java 21
- Spring Boot 4.1.1

## Comandos

Sempre usar o Maven Wrapper, nunca um `mvn` instalado globalmente:

- Linux/macOS: `./mvnw <goal>`
- Windows: `mvnw.cmd <goal>`

Exemplos: `./mvnw test`, `./mvnw clean verify`.

## Arquitetura

Arquitetura hexagonal (ports & adapters):

- **domain**: entidades, value objects e regras de negócio puras.
- **application**: casos de uso que orquestram o domínio através de portas (interfaces).
- **infrastructure**: adaptadores (web, persistência, mensageria etc.) que implementam
  as portas e dependem de frameworks.

Regra de dependência: `infrastructure` depende de `application`/`domain`, nunca o
contrário.

## Restrições de código

- **Domínio e aplicação sem Spring e sem JPA.** Essas camadas não podem importar
  anotações ou classes do Spring Framework, Spring Boot ou Jakarta Persistence. Frameworks
  só aparecem na camada `infrastructure`.
- **Dinheiro sempre em `BigDecimal`.** Nunca usar `double`/`float` para valores
  monetários.
- **Sem Lombok.** Escrever construtores, getters e `equals`/`hashCode` manualmente (ou
  usar `record` quando fizer sentido).
- **Nenhuma dependência nova no `pom.xml` sem pedir antes.** Qualquer necessidade de
  biblioteca adicional deve ser levantada e aprovada antes de ser adicionada.

## Fluxo de trabalho para cada mudança

1. Ler a spec indicada em `.specs/` (ver `.specs/README.md`).
2. Planejar a mudança e apresentar o plano.
3. Aguardar OK explícito antes de editar qualquer código.
4. Implementar, rodar os testes via Maven Wrapper e garantir que passam.
5. Mostrar o diff das mudanças antes de considerar a tarefa concluída.

Nenhuma etapa deve ser pulada, mesmo em mudanças pequenas.
