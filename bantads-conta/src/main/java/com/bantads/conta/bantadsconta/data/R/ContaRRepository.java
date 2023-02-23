package com.bantads.conta.bantadsconta.data.R;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bantads.conta.bantadsconta.model.R.ContaR;

public interface ContaRRepository extends JpaRepository<ContaR, Long> {

    //@Query("from conta where id_gerente = :gerenteId and ativo = false")
    List<ContaR> findByIdGerenteAndAtivo( Long gerenteId,boolean ativo);

    List<ContaR> findByIdGerente(Long gerenteId);

    Optional<ContaR> findByIdUsuario(Long userId);

}