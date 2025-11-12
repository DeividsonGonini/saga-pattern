package br.com.fiapstore.pedido.infra.orquestration;

import br.com.fiapstore.pedido.application.usecase.ConfirmarPedido;
import br.com.fiapstore.pedido.domain.exception.OperacaoInvalidaException;
import br.com.fiapstore.pedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiapstore.pedido.domain.exception.PercentualDescontoAcimaDoLimiteException;
import br.com.fiapstore.pedido.domain.usecase.ConfirmarPedidoUseCase;
import br.com.fiapstore.pedido.infra.messaging.entity.MensagemFila;
import com.google.gson.Gson;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class SagaPedido extends RouteBuilder {

    @Autowired
    private Gson gson;

    @Autowired
    private ConfirmarPedidoUseCase confirmarPedidoUseCase;

    @Override
    public void configure() throws Exception {
        //ROTA 1 - Monitora a fila de pedidos
        //Escuta a fila orquestracao_pedidos
        from("spring-rabbitmq:demo?queues=orquestracao_pedidos&routingKey=orquestracao_pedidos&arg.queue.durable=true")
                .routeId("rota-orquestracao-pedidos")
                //Log de pedido recebido
                .log(LoggingLevel.WARN, "Novo Pedido Recebido")
                //Converte a mensagem para a Entidade MensagemFila
                .bean(MensagemFila.class, "fromJsonToMap(${body})")
                //Converte a mensagem para uma String
                .convertBodyTo(String.class)
                //Posta em outra fila orquestracao_pagamentos
                .to("spring-rabbitmq:demo?queues=orquestracao_pagamentos&routingKey=orquestracao_pagamentos&arg.queue.durable=true")
                .log(LoggingLevel.WARN, "Postado na fila orquestracao_pagamentos")
                //Posta em outra fila orquestracao_entregas
                .to("spring-rabbitmq:demo?queues=orquestracao_entregas&routingKey=orquestracao_entregas&arg.queue.durable=true")
                .log(LoggingLevel.WARN, "Postado na fila orquestracao_entregas");


        //ROTA 2 - Monitora a fila de pedidos callback(orquestracao_pedidos_saga_reply)
        //Escuta a fila orquestracao_pedidos_saga_reply
        from("spring-rabbitmq:demo?queues=orquestracao_pedidos_saga_reply&routingKey=orquestracao_pedidos_saga_reply&arg.queue.durable=true")
                .routeId("Rota orquestracao_pedidos_saga_reply")
                //Log de pedido recebido
                .log(LoggingLevel.WARN, "mensagem na fila Pedidos Reply")
                .log(LoggingLevel.WARN, "${body}")
                //Executa uma ação conforme o conteúdo da mensagem
                .choice()
                //Caso seja uma atualização de pagamento
                    .when(body().contains("atualizacaoPagamento"))
                //publica na fila de orquestracao_entregas
                       .to("spring-rabbitmq:demo?queues=orquestracao_entregas&routingKey=orquestracao_entregas&arg.queue.durable=true")
                //Caso contenha o statusEntrega confirmada
                    .when(body().contains("\"statusEntrega\":\"CONFIRMADA\""))
                //Log de entrega confirmada
                            .log(LoggingLevel.WARN, "entrega confirmada")
                //Executa o metodo "confirmarPedido" da propria Classe SagaPedido que confirma o pedido
                            .bean(SagaPedido.class,"confirmarPedido(${body})")
                //Log de pedido confirmado
                            .log(LoggingLevel.WARN, "Pedido confirmado");

    }

    public void confirmarPedido(String json) throws PedidoNaoEncontradoException, PercentualDescontoAcimaDoLimiteException, OperacaoInvalidaException {
        HashMap<String, String> map = MensagemFila.fromJsonToMap(json);
        confirmarPedidoUseCase.executar(map.get("codigoPedido"));

    }



}
