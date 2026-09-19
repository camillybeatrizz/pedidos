package br.com.pedidos.apiPedidos.infrastructure.pedido.jpa;

import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;
import br.com.pedidos.apiPedidos.domain.pedido.StatusPedido;

import java.util.List;

class PedidoMapper {

    PedidoJpaEntity paraEntidade(Pedido pedido) {
        PedidoJpaEntity entidade = new PedidoJpaEntity(pedido.id(), pedido.status().name());
        for (ItemPedido item : pedido.itens()) {
            entidade.adicionar(new ItemJpaEntity(item.codigoProduto(), item.quantidade(), item.precoUnitario()));
        }
        return entidade;
    }

    Pedido paraDominio(PedidoJpaEntity entidade) {
        List<ItemPedido> itens = entidade.getItens().stream()
                .map(item -> new ItemPedido(item.getSku(), item.getQuantidade(), item.getPrecoUnitario()))
                .toList();
        return new Pedido(entidade.getId(), StatusPedido.valueOf(entidade.getStatus()), itens);
    }
}
