package br.com.fiapstore.cobranca.application.usecase;

import br.com.fiapstore.cobranca.application.dto.PagamentoDto;
import br.com.fiapstore.cobranca.domain.entity.Pagamento;
import br.com.fiapstore.cobranca.domain.exception.OperacaoInvalidaException;
import br.com.fiapstore.cobranca.domain.exception.PagamentoNaoEncontradoException;
import br.com.fiapstore.cobranca.domain.repository.IPagamentoDatabaseAdapter;
import br.com.fiapstore.cobranca.domain.repository.IPagamentoQueueAdapterOUT;
import br.com.fiapstore.cobranca.domain.usecase.IConfirmarPagamentoUseCase;
import br.com.fiapstore.cobranca.infra.messaging.PagamentoQueueAdapterOUT;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmarPagamento implements IConfirmarPagamentoUseCase {

    private final IPagamentoDatabaseAdapter iPagamentoDatabaseAdapter;
    private final IPagamentoQueueAdapterOUT pagamentoQueueAdapter;

    public ConfirmarPagamento(IPagamentoDatabaseAdapter iPagamentoDatabaseAdapter, IPagamentoQueueAdapterOUT iPagamentoQueueAdapterOUT) {
        this.iPagamentoDatabaseAdapter = iPagamentoDatabaseAdapter;
        this.pagamentoQueueAdapter = iPagamentoQueueAdapterOUT;
    }

    @Transactional
    public PagamentoDto executar(String codigoPagamento) throws PagamentoNaoEncontradoException, OperacaoInvalidaException {
        Pagamento pagamento = null;

        pagamento = this.iPagamentoDatabaseAdapter.findByCodigo(codigoPagamento);

        if(pagamento==null) throw new PagamentoNaoEncontradoException("Pagamento não encontrado");

        pagamento.confirmar();

        //Persiste no banco de dados
        pagamento = this.iPagamentoDatabaseAdapter.save(pagamento);

        //Publica na fila de Pagamentos Confirmados
        this.pagamentoQueueAdapter.publishPagamentoConfirmado(PagamentoQueueAdapterOUT.toMessage(pagamento));

        return PagamentoDto.toPagamentoDto(pagamento);
    }
}
