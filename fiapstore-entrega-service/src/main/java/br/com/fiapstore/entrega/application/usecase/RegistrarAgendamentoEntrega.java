package br.com.fiapstore.entrega.application.usecase;

import br.com.fiapstore.entrega.application.dto.EntregaDto;
import br.com.fiapstore.entrega.domain.entity.Entrega;
import br.com.fiapstore.entrega.domain.repository.IEntregaDatabaseAdapter;
import br.com.fiapstore.entrega.domain.usecase.IRegistrarAgendamentoEntregaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class RegistrarAgendamentoEntrega implements IRegistrarAgendamentoEntregaUseCase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IEntregaDatabaseAdapter entregaDatabaseAdapter;

    public RegistrarAgendamentoEntrega(IEntregaDatabaseAdapter entregaDatabaseAdapter) {
        this.entregaDatabaseAdapter = entregaDatabaseAdapter;

    }

    @Override
    public EntregaDto executar(EntregaDto entregaDto) {

        Entrega entrega = new Entrega(entregaDto.getCodigoPedido(),entregaDto.getCpf());

        entrega = entregaDatabaseAdapter.save(entrega);
        logger.info("Agendamento de entrega Registrada: {} / pedido: {}", entrega.getCodigo(), entrega.getCodigoPedido());

        return EntregaDto.toEntregaDto(entrega);

    }
}
