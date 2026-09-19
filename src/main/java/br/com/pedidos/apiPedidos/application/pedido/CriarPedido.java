package br.com.pedidos.apiPedidos.application.pedido;

import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;

import java.util.List;

public interface CriarPedido {

    Pedido criar(String clienteId, List<ItemPedido> itens);
}
