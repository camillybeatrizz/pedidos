package br.com.pedidos.apiPedidos.infrastructure.pedido.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido")
public class PedidoJpaEntity {

    @Id
    private UUID id;

    private String status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemJpaEntity> itens = new ArrayList<>();

    protected PedidoJpaEntity() {
    }

    public PedidoJpaEntity(UUID id, String status) {
        this.id = id;
        this.status = status;
    }

    public void adicionar(ItemJpaEntity item) {
        item.vincularPedido(this);
        itens.add(item);
    }

    public UUID getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public List<ItemJpaEntity> getItens() {
        return itens;
    }
}
