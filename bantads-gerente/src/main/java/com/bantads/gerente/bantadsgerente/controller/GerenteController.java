package com.bantads.gerente.bantadsgerente.controller;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bantads.gerente.bantadsgerente.data.GerenteRepository;
import com.bantads.gerente.bantadsgerente.dto.GerenteDTO;
import com.bantads.gerente.bantadsgerente.model.Gerente;
import com.bantads.gerente.bantadsgerente.utils.Security;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;

@CrossOrigin
@RestController
@RequestMapping("gerente")
public class GerenteController {
    @Autowired
    private GerenteRepository gerenteRepository;

    @Autowired
    private ModelMapper mapper;

    @GetMapping("/list")
    public ResponseEntity<List<GerenteDTO>> getGerente() {
        try {
            List<Gerente> gerentes = gerenteRepository.findAll();
            List<GerenteDTO> response = Arrays.asList(mapper.map(gerentes, GerenteDTO[].class));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GerenteDTO> getGerente(@PathVariable Long id) {
        try {
            Optional<Gerente> gerenteOp = gerenteRepository.findById(id);

            if (gerenteOp.isPresent()) {
                GerenteDTO response = mapper.map(gerenteOp.get(), GerenteDTO.class);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/por-cpf/{cpf}")
    public ResponseEntity<GerenteDTO> getGerentePorCpf(@PathVariable String cpf) {
        try {
            Gerente gerente = gerenteRepository.findByCpf(cpf);

            if (gerente != null) {
                GerenteDTO response = mapper.map(gerente, GerenteDTO.class);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/por-email/{email}")
    public ResponseEntity<GerenteDTO> getGerentePorEmail(@PathVariable String email) {
        try {
            Gerente gerente = gerenteRepository.findByEmail(email);

            if (gerente != null) {
                GerenteDTO response = mapper.map(gerente, GerenteDTO.class);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/novo")
    public ResponseEntity<GerenteDTO> cadastro(@RequestBody GerenteDTO gerente) {
        try {
            if (gerenteRepository.existsByCpf(gerente.getCpf()))
                return ResponseEntity.status(409).build();
                    Gerente g = new Gerente(
                    gerente.getNome(),
                    gerente.getEmail(),
                    Security.hash(gerente.getSenha()),
                    gerente.getCpf(),
                    gerente.getTelefone(),
                    gerente.getCargo());
                    Gerente novoGerente = gerenteRepository.save(g);
            if (novoGerente != null) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            } else {
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GerenteDTO> putGerente(@PathVariable Long id, @RequestBody GerenteDTO gerenteUp) {
        try {
            Optional<Gerente> gerenteOp = gerenteRepository.findById(id);
            if (gerenteOp.isPresent()) {
                Gerente gerente = gerenteOp.get();
                gerente.setEmail(gerenteUp.getEmail());
                gerente.setSenha(Security.hash(gerenteUp.getSenha()));
                gerente.setNome(gerenteUp.getNome());
                gerente.setCpf(gerenteUp.getCpf());
                gerente.setTelefone(gerenteUp.getTelefone());
                gerente = gerenteRepository.save(gerente);
                GerenteDTO response = mapper.map(gerente, GerenteDTO.class);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<GerenteDTO> deleteGerente(@PathVariable Long id) {
        try {
            Optional<Gerente> gerenteOp = gerenteRepository.findById(id);
            if (gerenteOp.isPresent()) {
                Gerente gerente = mapper.map(gerenteOp, Gerente.class);
                gerenteRepository.delete(gerente);
                GerenteDTO response = mapper.map(gerente, GerenteDTO.class);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}