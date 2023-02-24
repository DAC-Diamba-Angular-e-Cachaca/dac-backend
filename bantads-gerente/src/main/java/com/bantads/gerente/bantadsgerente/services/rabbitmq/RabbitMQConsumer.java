package com.bantads.gerente.bantadsgerente.services.rabbitmq;

import com.bantads.gerente.bantadsgerente.data.GerenteRepository;
import com.bantads.gerente.bantadsgerente.dto.GerenteDTO;
import com.bantads.gerente.bantadsgerente.model.Gerente;
import com.bantads.gerente.bantadsgerente.model.Usuario;
import com.bantads.gerente.bantadsgerente.utils.Security;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class RabbitMQConsumer {

    public static final String FILA_CREATE_GERENTE = "FILA_CREATE_GERENTE";
    public static final String FILA_ATRIBUI_CONTA_GERENTE = "FILA_ATRIBUI_CONTA_GERENTE";
    public static final String FILA_DELETE_GERENTE = "FILA_DELETE_GERENTE";
    private static final String FILA_AUTENTICACAO = "FILA_AUTENTICACAO";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GerenteRepository gerenteRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = FILA_CREATE_GERENTE)
    public void registraNovoCliente(String msg) throws JsonMappingException, JsonProcessingException {
        var gerente = objectMapper.readValue(msg, GerenteDTO.class);

        try {
            if (gerenteRepository.existsByCpf(gerente.getCpf())) {
                System.out.println("Já existe um gerente com este cpf");
                return;
            }

            if (gerenteRepository.findByEmail(gerente.getEmail()) != null) {
                System.out.println("Já existe um gerente com este email");
                return;
            }
            /*Long id, String nome, String email, String senha, String cpf, String telefone, String cargo */
            Gerente g = new Gerente(
                gerente.getId(),
                    gerente.getNome(),
                    gerente.getEmail(),
                    gerente.getSenha(),
                    gerente.getCpf(),
                    gerente.getTelefone(),
                    gerente.getCargo());
            Gerente novoGerente = gerenteRepository.save(g);
            System.out.println(objectMapper.writeValueAsString(novoGerente));
            var JSON = objectMapper.writeValueAsString(novoGerente.getId());
            rabbitTemplate.convertAndSend(FILA_ATRIBUI_CONTA_GERENTE, JSON);

            Usuario uAuth = new Usuario(g.getId().toString(), g.getEmail(), g.getSenha(),
                    "GERENTE", true);

            var jsonAUTH = objectMapper.writeValueAsString(uAuth);
            //
            System.out.println("enviado para auth com senha " + jsonAUTH);
            rabbitTemplate.convertAndSend(FILA_AUTENTICACAO, jsonAUTH);

        } catch (Exception e) {
            System.out.println(e);

        }

    }

    @RabbitListener(queues = FILA_DELETE_GERENTE)
    public void deleteGerente(String msg) throws JsonMappingException, JsonProcessingException {
        var gerente = objectMapper.readValue(msg, Long.class);

        try {
            System.out.println(msg);
            gerenteRepository.deleteById(gerente);
            Usuario uAuth = new Usuario(gerente.toString(), null, null,
            null, false);

            var jsonAUTH = objectMapper.writeValueAsString(uAuth);
            //
            System.out.println("enviado para auth com senha " + jsonAUTH);
            rabbitTemplate.convertAndSend(FILA_AUTENTICACAO, jsonAUTH);
        } catch (Exception e) {
            System.out.println(e);

        }

    }

}
