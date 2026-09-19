package br.com.pedidos.apiPedidos.domain.pedido;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Pedido(UUID id, StatusPedido status, List<ItemPedido> itens) {

    public Pedido {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(status, "status não pode ser nulo");
        Objects.requireNonNull(itens, "itens não pode ser nulo");
        itens = List.copyOf(itens);
    }

    public static Pedido novo() {
        return new Pedido(UUID.randomUUID(), StatusPedido.ABERTO, List.of());
    }

    public Pedido adicionarItem(ItemPedido item) {
        Objects.requireNonNull(item, "item não pode ser nulo");
        if (status != StatusPedido.ABERTO) {
            throw new PedidoFechadoException(
                    "Pedido " + id + " está " + status + " e não aceita novos itens");
        }
        List<ItemPedido> novosItens = new ArrayList<>(itens);
        novosItens.add(item);
        return new Pedido(id, status, novosItens);
    }

    public Pedido pagar() {
        if (status != StatusPedido.ABERTO) {
            throw new TransicaoInvalidaException(
                    "Pedido " + id + " está " + status + " e não pode ser pago");
        }
        return new Pedido(id, StatusPedido.PAGO, itens);
    }

    public Pedido cancelar() {
        if (status != StatusPedido.ABERTO) {
            throw new TransicaoInvalidaException(
                    "Pedido " + id + " está " + status + " e não pode ser cancelado");
        }
        return new Pedido(id, StatusPedido.CANCELADO, itens);
    }

    public BigDecimal total() {
        return itens.stream()
                .map(ItemPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
