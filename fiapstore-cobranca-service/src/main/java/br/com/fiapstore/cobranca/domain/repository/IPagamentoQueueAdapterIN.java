package br.com.fiapstore.cobranca.domain.repository;

import org.springframework.messaging.handler.annotation.Payload;

public interface IPagamentoQueueAdapterIN {
    void receive(@Payload String message);
}
