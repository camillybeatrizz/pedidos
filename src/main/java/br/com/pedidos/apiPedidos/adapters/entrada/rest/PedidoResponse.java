package br.com.pedidos.apiPedidos.adapters.entrada.rest;

import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PedidoResponse {

    private final UUID id;
    private final String clienteId;
    private final List<ItemResponse> itens;
    private final String status;
    private final BigDecimal total;

    public PedidoResponse(UUID id, String clienteId, List<ItemResponse> itens, String status, BigDecimal total) {
        this.id = id;
        this.clienteId = clienteId;
        this.itens = itens;
        this.status = status;
        this.total = total;
    }

    public static PedidoResponse de(Pedido pedido, String clienteId) {
        List<ItemResponse> itens = pedido.itens().stream()
                .map(ItemResponse::de)
                .toList();
        return new PedidoResponse(pedido.id(), clienteId, itens, pedido.status().name(), pedido.total());
    }

    public UUID getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public List<ItemResponse> getItens() {
        return itens;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public static class ItemResponse {

        private final String sku;
        private final int quantidade;
        private final BigDecimal precoUnitario;

        public ItemResponse(String sku, int quantidade, BigDecimal precoUnitario) {
            this.sku = sku;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }

        public static ItemResponse de(ItemPedido item) {
            return new ItemResponse(item.codigoProduto(), item.quantidade(), item.precoUnitario());
        }

        public String getSku() {
            return sku;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public BigDecimal getPrecoUnitario() {
            return precoUnitario;
        }
    }
}
