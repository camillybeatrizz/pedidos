package br.com.pedidos.apiPedidos.infrastructure.pedido.jpa;

import br.com.pedidos.apiPedidos.application.pedido.Pedidos;
import br.com.pedidos.apiPedidos.domain.pedido.Pedido;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class PedidosJpaAdapter implements Pedidos {

    private final PedidoSpringDataRepository repository;
    private final PedidoMapper mapper = new PedidoMapper();

    public PedidosJpaAdapter(PedidoSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity entidade = repository.save(mapper.paraEntidade(pedido));
        return mapper.paraDominio(entidade);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(UUID id) {
        return repository.findById(id).map(mapper::paraDominio);
    }
}
