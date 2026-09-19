package br.com.pedidos.apiPedidos.domain.pedido;

public class PedidoFechadoException extends RuntimeException {

    public PedidoFechadoException(String message) {
        super(message);
    }
}
