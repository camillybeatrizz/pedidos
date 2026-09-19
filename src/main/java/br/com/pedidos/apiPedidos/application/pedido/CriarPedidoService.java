package br.com.pedidos.apiPedidos.application.pedido;

import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;

import java.util.List;
import java.util.Objects;

public class CriarPedidoService implements CriarPedido {

    private final Pedidos pedidos;

    public CriarPedidoService(Pedidos pedidos) {
        this.pedidos = Objects.requireNonNull(pedidos, "pedidos não pode ser nulo");
    }

    @Override
    public Pedido criar(String clienteId, List<ItemPedido> itens) {
        Objects.requireNonNull(itens, "itens não pode ser nulo");
        if (itens.isEmpty()) {
            throw new ItensObrigatoriosException("um pedido precisa de ao menos um item");
        }

        Pedido pedido = Pedido.novo();
        for (ItemPedido item : itens) {
            pedido = pedido.adicionarItem(item);
        }

        return pedidos.salvar(pedido);
    }
}
