package br.com.pedidos.apiPedidos.adapters.entrada.rest;

import br.com.pedidos.apiPedidos.application.pedido.CriarPedido;
import br.com.pedidos.apiPedidos.application.pedido.ItensObrigatoriosException;
import br.com.pedidos.apiPedidos.domain.pedido.ItemInvalidoException;
import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PedidoControllerTest {

    private final CriarPedido criarPedido = Mockito.mock(CriarPedido.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new PedidoController(criarPedido))
            .setControllerAdvice(new PedidoExceptionHandler())
            .build();

    @BeforeEach
    void resetMock() {
        Mockito.reset(criarPedido);
    }

    @Test
    void criar_comItemValido_devolve201ComTotal() throws Exception {
        ItemPedido cafe500 = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));
        Pedido pedido = Pedido.novo().adicionarItem(cafe500);
        when(criarPedido.criar(anyString(), any())).thenReturn(pedido);

        String corpo = """
                {"clienteId":"c-1","itens":[{"sku":"CAFE-500","quantidade":2,"precoUnitario":18.90}]}
                """;

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.total").value(37.80));
    }

    @Test
    void criar_comQuantidadeZero_devolve422() throws Exception {
        when(criarPedido.criar(anyString(), any())).thenThrow(new ItemInvalidoException("quantidade deve ser positiva"));

        String corpo = """
                {"clienteId":"c-1","itens":[{"sku":"CAFE-500","quantidade":0,"precoUnitario":18.90}]}
                """;

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void criar_comItensVazios_devolve422() throws Exception {
        when(criarPedido.criar(anyString(), any())).thenThrow(new ItensObrigatoriosException("um pedido precisa de ao menos um item"));

        String corpo = """
                {"clienteId":"c-1","itens":[]}
                """;

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void criar_comJsonMalformado_devolve400() throws Exception {
        String corpoMalformado = "{ isto nao e json valido ";

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpoMalformado))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_comClienteIdAusente_devolve400() throws Exception {
        String corpo = """
                {"itens":[{"sku":"CAFE-500","quantidade":2,"precoUnitario":18.90}]}
                """;

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
    }
}
