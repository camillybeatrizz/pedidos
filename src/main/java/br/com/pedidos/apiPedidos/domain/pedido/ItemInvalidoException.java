package br.com.pedidos.apiPedidos.domain.pedido;

public class ItemInvalidoException extends RuntimeException {

    public ItemInvalidoException(String message) {
        super(message);
    }
}
