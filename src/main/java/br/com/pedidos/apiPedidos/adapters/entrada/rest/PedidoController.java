package br.com.pedidos.apiPedidos.adapters.entrada.rest;

import br.com.pedidos.apiPedidos.application.pedido.CriarPedido;
import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final CriarPedido criarPedido;

    public PedidoController(CriarPedido criarPedido) {
        this.criarPedido = criarPedido;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        List<ItemPedido> itens = request.getItens().stream()
                .map(item -> new ItemPedido(item.getSku(), item.getQuantidade(), item.getPrecoUnitario()))
                .toList();

        Pedido pedido = criarPedido.criar(request.getClienteId(), itens);

        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.de(pedido, request.getClienteId()));
    }
}
