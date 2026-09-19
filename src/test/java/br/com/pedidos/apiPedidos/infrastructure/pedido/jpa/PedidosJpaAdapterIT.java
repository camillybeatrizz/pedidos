package br.com.pedidos.apiPedidos.infrastructure.pedido.jpa;

import br.com.pedidos.apiPedidos.domain.pedido.ItemPedido;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;
import br.com.pedidos.apiPedidos.domain.pedido.StatusPedido;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PedidosJpaAdapterIT {

    @Autowired
    private PedidosJpaAdapter adapter;

    @Autowired
    private PedidoSpringDataRepository repository;

    private UUID idCriado;

    @AfterEach
    void limpar() {
        if (idCriado != null) {
            repository.deleteById(idCriado);
        }
    }

    @Test
    void salvarERecuperarPedidoPorUuid_emNovaTransacao() {
        ItemPedido cafe500 = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));
        Pedido pedido = Pedido.novo().adicionarItem(cafe500);

        Pedido salvo = adapter.salvar(pedido);
        idCriado = salvo.id();

        Optional<Pedido> recuperado = adapter.buscarPorId(salvo.id());

        assertTrue(recuperado.isPresent());
        assertEquals(StatusPedido.ABERTO, recuperado.get().status());
        assertEquals(List.of(cafe500), recuperado.get().itens());
        assertEquals(0, new BigDecimal("37.80").compareTo(recuperado.get().total()));
    }
}
