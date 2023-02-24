package com.bantads.conta.bantadsconta.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bantads.conta.bantadsconta.DTO.ContaDTO;
import com.bantads.conta.bantadsconta.data.R.ContaRRepository;
import com.bantads.conta.bantadsconta.model.Conta;
import com.bantads.conta.bantadsconta.model.R.ContaR;

@CrossOrigin
@RestController
@RequestMapping("conta")
public class ContaController {
    @Autowired
    private ContaRRepository contaRepository;

    @Autowired
    private ModelMapper mapper;

    @GetMapping("/list")
    public ResponseEntity<List<ContaDTO>> getContas() {
        try {
            List<ContaR> contas = contaRepository.findAll();
            List<ContaDTO> response = Arrays.asList(mapper.map(contas, ContaDTO[].class));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/por-usuario/{userId}")
    public ResponseEntity<ContaDTO> getContaPorUserId(@PathVariable Long userId) {
        try {
            Optional<ContaR> contaOp = contaRepository.findByIdUsuario(userId);

            if (contaOp.isPresent()) {
                ContaDTO response = mapper.map(contaOp.get(), ContaDTO.class);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/por-gerente/{gerenteId}")
    public ResponseEntity<List<ContaDTO>> getContaPorGerenteId(@PathVariable Long gerenteId) {
        try {
			System.out.println("gerente");
            List<ContaR> contas = contaRepository.findByIdGerente(gerenteId);
			//System.out.println(contas);
            List<ContaDTO> response = Arrays.asList(mapper.map(contas, ContaDTO[].class));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
			System.out.println(e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/melhores/{gerenteId}")
    public ResponseEntity<List<ContaDTO>> getMelhoresPorGerenteId(@PathVariable Long gerenteId) {
        try {
			System.out.println("gerente");
            List<ContaR> contas = contaRepository.findByIdGerenteOrderBySaldoDesc(gerenteId);
			//System.out.println(contas);
            List<ContaR> bestContas = contas.stream().limit(5).collect(Collectors.toList());
            List<ContaDTO> response = Arrays.asList(mapper.map(bestContas, ContaDTO[].class));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
			System.out.println(e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/pendentes/{gerenteId}")
    public ResponseEntity<List<ContaDTO>> getPendentesPorGerenteId(@PathVariable Long gerenteId) {
        try {
            List<ContaR> contas = contaRepository.findByIdGerenteAndAtivo(gerenteId,false);
            List<ContaDTO> response = Arrays.asList(mapper.map(contas, ContaDTO[].class));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaDTO> getConta(@PathVariable Long id) {
        try {
            Optional<ContaR> contaOp = contaRepository.findById(id);

            if (contaOp.isPresent()) {
                ContaDTO response = mapper.map(contaOp.get(), ContaDTO.class);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

  

    // @PostMapping("/novo")
    // ResponseEntity<ContaDTO> cadastro(@RequestBody ContaDTO contaDto) {
    // try {
    // ContaR c = new ContaR(
    // contaDto.getIdUsuario(),
    // contaDto.getData(),
    // false,
    // contaDto.getSaldo(),
    // contaDto.getIdGerente(),
    // contaDto.getSalario()
    // );
    // ContaR conta = contaRepository.save(c);
    // if (conta != null) {
    // ContaDTO response = mapper.map(conta, ContaDTO.class);
    // return new ResponseEntity<ContaDTO>(HttpStatus.CREATED);
    // } else {
    // return ResponseEntity.status(401).build();
    // }
    // } catch (Exception e) {
    // return ResponseEntity.status(500).build();
    // }
    // }

    // @PutMapping("/{id}")
    // public ResponseEntity<ContaDTO> putConta(@PathVariable Long id, @RequestBody
    // ContaDTO contaUp) {
    // try {
    // Optional<Conta> contaOp = contaRepository.findById(id);
    // if (contaOp.isPresent()) {
    // Conta conta = contaOp.get();
    // conta.setIdUsuario(contaUp.getIdUsuario());
    // conta.setData(contaUp.getData());
    // conta.setAtivo(contaUp.isAtivo());
    // conta.setSaldo(contaUp.getSaldo());
    // conta.setIdGerente(contaUp.getIdGerente());
    // conta.setSalario(contaUp.getSalario());
    // conta.setRejeitadoMotivo(contaUp.getRejeitadoMotivo());
    // conta.setRejeitadoData(contaUp.getRejeitadoData());
    // conta = contaRepository.save(conta);
    // ContaDTO response = mapper.map(conta, ContaDTO.class);
    // return ResponseEntity.ok(response);
    // } else {
    // return ResponseEntity.notFound().build();
    // }
    // } catch (Exception e) {
    // return ResponseEntity.status(500).build();
    // }
    // }

}