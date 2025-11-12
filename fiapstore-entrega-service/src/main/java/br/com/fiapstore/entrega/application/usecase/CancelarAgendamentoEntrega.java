package br.com.fiapstore.entrega.application.usecase;

import br.com.fiapstore.entrega.application.dto.EntregaDto;
import br.com.fiapstore.entrega.domain.entity.Entrega;
import br.com.fiapstore.entrega.domain.exception.EntregaNaoEncontradaException;
import br.com.fiapstore.entrega.domain.exception.OperacaoInvalidaException;
import br.com.fiapstore.entrega.domain.repository.IEntregaDatabaseAdapter;
import br.com.fiapstore.entrega.domain.usecase.ICancelarAgendamentoEntregaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelarAgendamentoEntrega implements ICancelarAgendamentoEntregaUseCase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IEntregaDatabaseAdapter entregaDatabaseAdapter;

    public CancelarAgendamentoEntrega(IEntregaDatabaseAdapter entregaDatabaseAdapter) {
        this.entregaDatabaseAdapter = entregaDatabaseAdapter;
    }

    @Transactional
    public EntregaDto executar(String codigo) throws OperacaoInvalidaException, EntregaNaoEncontradaException {

        Entrega entrega =null;

        entrega =  this.entregaDatabaseAdapter.findByCodigo(codigo);

        if(entrega==null) throw new EntregaNaoEncontradaException("Entrega não encontrada");

        entrega.cancelarEntrega();

        entrega = entregaDatabaseAdapter.save(entrega);
        logger.info("Agendamento de entrega Cancelado: {} / pedido: {}", entrega.getCodigo(), entrega.getCodigoPedido());

        return EntregaDto.toEntregaDto(entrega);

    }
}
