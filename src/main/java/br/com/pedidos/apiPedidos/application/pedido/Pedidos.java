package br.com.pedidos.apiPedidos.application.pedido;

import br.com.pedidos.apiPedidos.domain.pedido.Pedido;

import java.util.Optional;
import java.util.UUID;

public interface Pedidos {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(UUID id);
}
