package br.com.fiapstore.cobranca.application.usecase;

import br.com.fiapstore.cobranca.application.dto.PagamentoDto;
import br.com.fiapstore.cobranca.domain.entity.Pagamento;
import br.com.fiapstore.cobranca.domain.exception.PagamentoNaoEncontradoException;
import br.com.fiapstore.cobranca.domain.repository.IPagamentoDatabaseAdapter;
import br.com.fiapstore.cobranca.domain.usecase.IConsultarPagamentoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsultarPagamento implements IConsultarPagamentoUseCase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IPagamentoDatabaseAdapter iPagamentoDatabaseAdapter;

    public ConsultarPagamento(IPagamentoDatabaseAdapter iPagamentoDatabaseAdapter) {
        this.iPagamentoDatabaseAdapter = iPagamentoDatabaseAdapter;
    }


    @Override
    public PagamentoDto executar(String codigo) throws PagamentoNaoEncontradoException {
        Pagamento pagamento;

        pagamento = iPagamentoDatabaseAdapter.findByCodigo(codigo);

        if(pagamento==null) throw new PagamentoNaoEncontradoException("Pagamento não encontrado");
        logger.info("Pagamento Consultado: {} / pedido: {}", pagamento.getCodigo(), pagamento.getCodigoPedido());

        return PagamentoDto.toPagamentoDto(pagamento);
    }
}
