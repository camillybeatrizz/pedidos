package br.com.pedidos.apiPedidos.infrastructure.pedido.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PedidoSpringDataRepository extends JpaRepository<PedidoJpaEntity, UUID> {
}
