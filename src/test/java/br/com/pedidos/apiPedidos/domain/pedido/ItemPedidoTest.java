package br.com.pedidos.apiPedidos.domain.pedido;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPedidoTest {

    @Test
    void criarItemComQuantidadeEPrecoPositivos_ok() {
        ItemPedido item = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));

        assertEquals("CAFE-500", item.codigoProduto());
        assertEquals(2, item.quantidade());
        assertEquals(0, new BigDecimal("18.90").compareTo(item.precoUnitario()));
    }

    @Test
    void quantidadeZero_lancaExcecao() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", 0, new BigDecimal("18.90")));
    }

    @Test
    void quantidadeNegativa_lancaExcecao() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", -1, new BigDecimal("18.90")));
    }

    @Test
    void precoZero_lancaExcecao() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", 2, BigDecimal.ZERO));
    }

    @Test
    void precoNegativo_lancaExcecao() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", 2, new BigDecimal("-1.00")));
    }

    @Test
    void precoUnitarioESubtotalSaoBigDecimal() {
        ItemPedido item = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));

        assertInstanceOf(BigDecimal.class, item.precoUnitario());
        assertInstanceOf(BigDecimal.class, item.subtotal());
    }

    @Test
    void subtotal_calculaQuantidadeVezesPreco() {
        ItemPedido item = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));

        assertEquals(0, new BigDecimal("37.80").compareTo(item.subtotal()));
    }
}
