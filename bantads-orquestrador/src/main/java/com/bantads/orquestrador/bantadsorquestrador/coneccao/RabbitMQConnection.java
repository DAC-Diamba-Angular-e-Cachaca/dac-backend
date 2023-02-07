package com.bantads.orquestrador.bantadsorquestrador.coneccao;

import com.bantads.orquestrador.bantadsorquestrador.constantes.RabbitmqConstantes;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;

public class RabbitMQConnection {

	private static final String NOME_EXCHANGE = "amq.direct";

	private AmqpAdmin amqpAdmin;

	public RabbitMQConnection(AmqpAdmin amqpAdmin) {
		this.amqpAdmin = amqpAdmin;
		adiciona();

	}

	private Queue fila(String nomeFila) {
		return new Queue(nomeFila, true, false, false);
	}

	private DirectExchange trocaDireta() {
		return new DirectExchange(NOME_EXCHANGE);
	}

	private Binding relacionamento(Queue fila, DirectExchange troca) {
		return new Binding(fila.getName(), Binding.DestinationType.QUEUE, troca.getName(), fila.getName(), null);
	}

	// está função é executada assim que nossa classe é instanciada pelo Spring,
	// devido a anotação @Component
	@javax.annotation.PostConstruct
	private void adiciona() {
		Queue FILA_REGISTRO_CLIENTE = this.fila(RabbitmqConstantes.FILA_REGISTRO_CLIENTE);
		Queue FILA_AUTENTICACAO_CLIENTE = this.fila(RabbitmqConstantes.FILA_AUTENTICACAO_CLIENTE);
		Queue FILA_REGISTRO_CONTA_CLIENTE = this.fila(RabbitmqConstantes.FILA_REGISTRO_CONTA_CLIENTE);
		Queue FILA_REGISTRO_GERENTE_CLIENTE = this.fila(RabbitmqConstantes.FILA_REGISTRO_GERENTE_CLIENTE);

		DirectExchange troca = this.trocaDireta();

		Binding LIGACAO_FILA_REGISTRO_CLIENTE = this.relacionamento(FILA_REGISTRO_CLIENTE, troca);
		Binding LIGACAO_FILA_AUTENTICACAO_CLIENTE = this.relacionamento(FILA_AUTENTICACAO_CLIENTE, troca);
		Binding LIGACAO_FILA_REGISTRO_CONTA_CLIENTE = this.relacionamento(FILA_REGISTRO_CONTA_CLIENTE, troca);
		Binding LIGACAO_FILA_REGISTRO_GERENTE_CLIENTE = this.relacionamento(FILA_REGISTRO_GERENTE_CLIENTE, troca);

		// Criando as filas no RabbitMQ
		this.amqpAdmin.declareQueue(FILA_REGISTRO_CLIENTE);
		this.amqpAdmin.declareQueue(FILA_AUTENTICACAO_CLIENTE);
		this.amqpAdmin.declareQueue(FILA_REGISTRO_CONTA_CLIENTE);
		this.amqpAdmin.declareQueue(FILA_REGISTRO_GERENTE_CLIENTE);

		this.amqpAdmin.declareExchange(troca);

		this.amqpAdmin.declareBinding(LIGACAO_FILA_REGISTRO_CLIENTE);
		this.amqpAdmin.declareBinding(LIGACAO_FILA_AUTENTICACAO_CLIENTE);
		this.amqpAdmin.declareBinding(LIGACAO_FILA_REGISTRO_CONTA_CLIENTE);
		this.amqpAdmin.declareBinding(LIGACAO_FILA_REGISTRO_GERENTE_CLIENTE);
	}
}