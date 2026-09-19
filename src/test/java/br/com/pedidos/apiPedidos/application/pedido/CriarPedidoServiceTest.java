package br.com.pedidos.apiPedidos.application.pedido;

import br.com.pedidos.apiPedidos.domain.pedido.ItemInvalidoException;
import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;
import br.com.pedidos.apiPedidos.domain.pedido.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CriarPedidoServiceTest {

    private static final ItemPedido CAFE_500 = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));

    private static class PedidosEmMemoria implements Pedidos {
        private final List<Pedido> salvos = new ArrayList<>();

        @Override
        public Pedido salvar(Pedido pedido) {
            salvos.add(pedido);
            return pedido;
        }
    }

    @Test
    void criar_comClienteEItens_devolvePedidoComItens() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        Pedido pedido = service.criar("c-1", List.of(CAFE_500));

        assertEquals(List.of(CAFE_500), pedido.itens());
    }

    @Test
    void criar_pedidoNasceAbertoComTotalDerivado() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        Pedido pedido = service.criar("c-1", List.of(CAFE_500));

        assertEquals(StatusPedido.ABERTO, pedido.status());
        assertEquals(0, new BigDecimal("37.80").compareTo(pedido.total()));
    }

    @Test
    void criar_comListaVazia_lancaExcecaoENaoSalva() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        assertThrows(ItensObrigatoriosException.class, () -> service.criar("c-1", List.of()));
        assertTrue(pedidos.salvos.isEmpty());
    }

    @Test
    void criar_naoRecebeItemInvalido_domainJaValidaAntesDoUseCase() {
        // O caso de uso não duplica a validação de quantidade/preço: um ItemPedido
        // inválido nem chega a existir para ser passado a CriarPedidoService.criar(...).
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", 0, new BigDecimal("18.90")));
    }

    @Test
    void criar_devolveExatamenteOQuePedidosSalvarRetornou() {
        Pedido substituto = Pedido.novo();
        Pedidos pedidosQueTrocaOResultado = pedido -> substituto;
        CriarPedidoService service = new CriarPedidoService(pedidosQueTrocaOResultado);

        Pedido resultado = service.criar("c-1", List.of(CAFE_500));

        assertSame(substituto, resultado);
    }
}
