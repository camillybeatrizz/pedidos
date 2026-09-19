package br.com.pedidos.apiPedidos.domain.pedido;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DominioSemFrameworkTest {

    private static final List<String> PACOTES_PROIBIDOS = List.of(
            "org.springframework",
            "jakarta.persistence"
    );

    @Test
    void dominioNaoDependeDeSpringOuJpa() throws IOException {
        Path raizDominio = Path.of("src", "main", "java", "br", "com", "pedidos", "apiPedidos", "domain");

        try (Stream<Path> arquivos = Files.walk(raizDominio)) {
            List<Path> arquivosJava = arquivos.filter(p -> p.toString().endsWith(".java")).toList();

            assertFalse(arquivosJava.isEmpty(), "Nenhum arquivo de domínio encontrado em " + raizDominio);

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
