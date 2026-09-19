package br.com.pedidos.apiPedidos.config;

import br.com.pedidos.apiPedidos.application.pedido.CriarPedido;
import br.com.pedidos.apiPedidos.application.pedido.CriarPedidoService;
import br.com.pedidos.apiPedidos.application.pedido.Pedidos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public CriarPedido criarPedido(Pedidos pedidos) {
        return new CriarPedidoService(pedidos);
    }
}
