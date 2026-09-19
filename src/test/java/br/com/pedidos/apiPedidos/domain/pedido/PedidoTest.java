package br.com.pedidos.apiPedidos.domain.pedido;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoTest {

    private static final ItemPedido CAFE_500 = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));

    @Test
    void novo_comecaAberto() {
        Pedido pedido = Pedido.novo();

        assertEquals(StatusPedido.ABERTO, pedido.status());
    }

    @Test
    void novo_comecaSemItens() {
        Pedido pedido = Pedido.novo();

        assertTrue(pedido.itens().isEmpty());
    }

    @Test
    void novo_temIdNaoNulo() {
        Pedido pedido = Pedido.novo();

        assertNotNull(pedido.id());
    }

    @Test
    void novo_geraIdsDiferentesEntreChamadas() {
        Pedido primeiro = Pedido.novo();
        Pedido segundo = Pedido.novo();

        assertNotEquals(primeiro.id(), segundo.id());
    }

    @Test
    void adicionarItem_retornaNovaInstancia() {
        Pedido original = Pedido.novo();

        Pedido comItem = original.adicionarItem(CAFE_500);

        assertNotSame(original, comItem);
    }

    @Test
    void adicionarItem_naoModificaPedidoOriginal() {
        Pedido original = Pedido.novo();

        original.adicionarItem(CAFE_500);

        assertTrue(original.itens().isEmpty());
    }

    @Test
    void adicionarItem_novoPedidoContemItemAdicionado() {
        Pedido pedido = Pedido.novo().adicionarItem(CAFE_500);

        assertEquals(List.of(CAFE_500), pedido.itens());
    }

    @Test
    void adicionarItem_emPedidoPago_lancaExcecao() {
        Pedido pago = Pedido.novo().adicionarItem(CAFE_500).pagar();

        assertThrows(PedidoFechadoException.class, () -> pago.adicionarItem(CAFE_500));
    }

    @Test
    void adicionarItem_emPedidoCancelado_lancaExcecao() {
        Pedido cancelado = Pedido.novo().cancelar();

        assertThrows(PedidoFechadoException.class, () -> cancelado.adicionarItem(CAFE_500));
    }

    @Test
    void total_pedidoSemItens_eZero() {
        Pedido pedido = Pedido.novo();

        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.total()));
    }

    @Test
    void total_exemploDaAula_cafe500DuasUnidades() {
        Pedido pedido = Pedido.novo().adicionarItem(CAFE_500);

        assertEquals(0, new BigDecimal("37.80").compareTo(pedido.total()));
    }

    @Test
    void total_somaSubtotaisDeMultiplosItens() {
        ItemPedido pao = new ItemPedido("PAO-FRANCES", 5, new BigDecimal("0.80"));
        Pedido pedido = Pedido.novo().adicionarItem(CAFE_500).adicionarItem(pao);

        assertEquals(0, new BigDecimal("41.80").compareTo(pedido.total()));
    }

    @Test
    void itens_naoPodemSerModificadosPorFora() {
        Pedido pedido = Pedido.novo().adicionarItem(CAFE_500);

        assertThrows(UnsupportedOperationException.class,
                () -> pedido.itens().add(CAFE_500));
    }

    @Test
    void construirPedido_comListaMutavel_copiaDefensivamenteNaEntrada() {
        List<ItemPedido> itensMutaveis = new ArrayList<>();
        itensMutaveis.add(CAFE_500);

        Pedido pedido = new Pedido(java.util.UUID.randomUUID(), StatusPedido.ABERTO, itensMutaveis);
        itensMutaveis.add(new ItemPedido("PAO-FRANCES", 1, new BigDecimal("0.80")));

        assertEquals(1, pedido.itens().size());
    }

    @Test
    void pagar_pedidoAberto_resultaEmPago() {
        Pedido pago = Pedido.novo().pagar();

        assertEquals(StatusPedido.PAGO, pago.status());
    }

    @Test
    void cancelar_pedidoAberto_resultaEmCancelado() {
        Pedido cancelado = Pedido.novo().cancelar();

        assertEquals(StatusPedido.CANCELADO, cancelado.status());
    }

    @Test
    void pagar_pedidoJaPago_lancaExcecao() {
        Pedido pago = Pedido.novo().pagar();

        assertThrows(TransicaoInvalidaException.class, pago::pagar);
    }

    @Test
    void pagar_pedidoCancelado_lancaExcecao() {
        Pedido cancelado = Pedido.novo().cancelar();

        assertThrows(TransicaoInvalidaException.class, cancelado::pagar);
    }

    @Test
    void cancelar_pedidoJaCancelado_lancaExcecao() {
        Pedido cancelado = Pedido.novo().cancelar();

        assertThrows(TransicaoInvalidaException.class, cancelado::cancelar);
    }

    @Test
    void cancelar_pedidoPago_lancaExcecao() {
        Pedido pago = Pedido.novo().pagar();

        assertThrows(TransicaoInvalidaException.class, pago::cancelar);
    }
}
