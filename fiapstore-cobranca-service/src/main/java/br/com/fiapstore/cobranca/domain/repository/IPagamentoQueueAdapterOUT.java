package br.com.fiapstore.cobranca.domain.repository;


public interface IPagamentoQueueAdapterOUT {
    //Publica na fila quando um pedido for recebido
    void publishPagamentoPendente(String pagamentoJson);
    //Publica na fila quando tiver um estímulo do endpoint de confirma pagamento que o pagamento foi pago
    void publishPagamentoConfirmado(String pagamentoJson);

}
