/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bantads.orquestrador.bantadsorquestrador.controller;

import java.sql.Date;
import java.util.Random;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bantads.orquestrador.bantadsorquestrador.constantes.RabbitmqConstantes;
import com.bantads.orquestrador.bantadsorquestrador.dto.ClienteCompletoDTO;
import com.bantads.orquestrador.bantadsorquestrador.dto.ClienteDTO;
import com.bantads.orquestrador.bantadsorquestrador.dto.ContaDTO;
import com.bantads.orquestrador.bantadsorquestrador.dto.ResponseFormat;
import com.bantads.orquestrador.bantadsorquestrador.dto.ValidaReponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author leonardozanotti
 */
@CrossOrigin
@RestController
@RequestMapping("orquestrador")
public class OrquestradorController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/cliente")
    ResponseEntity<?> enfileirarCliente(@RequestBody ClienteCompletoDTO clienteCompleto)
            throws JsonProcessingException {
        Long id = new Random().nextLong();
        if (id < 0) {
            id = id * -1;
        }

        /*
         * private Long id;
         * private Long idUsuario;
         * private Date data;
         * private boolean ativo;
         * private double saldo;
         * private Long idGerente;
         * private double salario;
         * private String rejeitadoMotivo;
         * private Date rejeitadoData;
         */

        ContaDTO contaDTO = new ContaDTO(null, id, null, false, clienteCompleto.saldo, null, clienteCompleto.salario,
                null, null);

        /*
         * long id,String nome, String email, String senha, String cpf, String telefone,
         * int estado, int cidade,
         * String cep, String rua, int numero, String complemento, String cargo, boolean
         * ativo
         */
        ClienteDTO clienteDto = new ClienteDTO(id, clienteCompleto.nome, clienteCompleto.email, clienteCompleto.senha,
                clienteCompleto.cpf, clienteCompleto.telefone, clienteCompleto.estado, clienteCompleto.cidade,
                clienteCompleto.cep, clienteCompleto.rua, clienteCompleto.numero, clienteCompleto.complemento,
                clienteCompleto.cargo, clienteCompleto.ativo);
        ValidaReponse verificacao = clienteDto.ValidaCliente();
        // return new ResponseEntity<>( objectMapper.writeValueAsString(verificacao),
        // HttpStatus.CREATED);
        if (verificacao.getStatus() == true) {
            var jsonCliente = objectMapper.writeValueAsString(clienteDto);
            rabbitTemplate.convertAndSend(RabbitmqConstantes.FILA_REGISTRO_CLIENTE, jsonCliente);
            rabbitTemplate.convertAndSend(RabbitmqConstantes.FILA_AUTENTICACAO_CLIENTE, jsonCliente);

            var jsonConta = objectMapper.writeValueAsString(contaDTO);
            rabbitTemplate.convertAndSend(RabbitmqConstantes.FILA_REGISTRO_CONTA_CLIENTE, jsonConta);
            System.out.println(jsonCliente);
            System.out.println(jsonConta);

            var jsonResponse = objectMapper
                    .writeValueAsString(new ResponseFormat(true, "Cliente e Conta enfileirados"));
            return new ResponseEntity<>(jsonResponse, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(verificacao.getMenssagem(), HttpStatus.OK);
        }

    }

    
}
