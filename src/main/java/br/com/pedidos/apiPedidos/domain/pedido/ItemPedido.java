package br.com.pedidos.apiPedidos.domain.pedido;

import java.math.BigDecimal;
import java.util.Objects;

public record ItemPedido(String codigoProduto, int quantidade, BigDecimal precoUnitario) {

    public ItemPedido {
        Objects.requireNonNull(codigoProduto, "codigoProduto não pode ser nulo");
        if (codigoProduto.isBlank()) {
            throw new ItemInvalidoException("codigoProduto não pode ser vazio");
        }
        if (quantidade <= 0) {
            throw new ItemInvalidoException("quantidade deve ser positiva, recebido: " + quantidade);
        }
        Objects.requireNonNull(precoUnitario, "precoUnitario não pode ser nulo");
        if (precoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ItemInvalidoException("precoUnitario deve ser positivo, recebido: " + precoUnitario);
        }
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
