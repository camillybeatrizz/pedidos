package br.com.pedidos.apiPedidos.application.pedido;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AplicacaoSemFrameworkTest {

    private static final List<String> PACOTES_PROIBIDOS = List.of(
            "org.springframework",
            "jakarta.persistence",
            "jakarta.servlet"
    );

    @Test
    void aplicacaoNaoDependeDeSpringJpaOuServlet() throws IOException {
        Path raizAplicacao = Path.of("src", "main", "java", "br", "com", "pedidos", "apiPedidos", "application");

        try (Stream<Path> arquivos = Files.walk(raizAplicacao)) {
            List<Path> arquivosJava = arquivos.filter(p -> p.toString().endsWith(".java")).toList();

            assertFalse(arquivosJava.isEmpty(), "Nenhum arquivo de aplicação encontrado em " + raizAplicacao);

            for (Path arquivo : arquivosJava) {
                String conteudo = Files.readString(arquivo);
                for (String pacoteProibido : PACOTES_PROIBIDOS) {
                    assertFalse(conteudo.contains(pacoteProibido),
                            arquivo + " não deveria depender de " + pacoteProibido);
                }
            }
        }
    }
}
