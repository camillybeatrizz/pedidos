package br.com.pedidos.apiPedidos.domain.pedido;

public class TransicaoInvalidaException extends RuntimeException {

    public TransicaoInvalidaException(String message) {
        super(message);
    }
}
