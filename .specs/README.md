# .specs/

Esta pasta guarda as specs de cada mudança feita no projeto. Cada spec descreve uma
única mudança e segue o formato:

- **Contexto**: por que essa mudança é necessária, o que já existe hoje.
- **Tarefa**: o que deve ser feito, de forma objetiva.
- **Regras**: restrições específicas dessa tarefa, além das regras gerais já definidas
  em `AGENTS.md`.
- **Definição de pronto**: critérios verificáveis para considerar a tarefa concluída
  (ex.: testes específicos passando, comportamento observável).

## Quando esta pasta entra em contexto

O conteúdo de `.specs/` **não** é lido por padrão. Ele só deve ser carregado no
contexto do agente quando o agente for explicitamente instruído a ler uma spec
específica (ex.: "leia `.specs/0001-nome-da-spec.md`"). Isso mantém o contexto de cada
tarefa enxuto e evita que specs antigas ou não relacionadas influenciem uma mudança
atual.
