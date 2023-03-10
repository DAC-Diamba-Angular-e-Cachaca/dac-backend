package com.bantads.conta.bantadsconta.data.CUD;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bantads.conta.bantadsconta.model.Conta;
import com.bantads.conta.bantadsconta.model.CUD.ContaCUD;
@Transactional
public interface ContaCUDRepository extends JpaRepository<ContaCUD, Long> {
    //QUERY PARA BUSCAR O GERENTE COM MENOR NUMERO DE CLIENTES PARA ESCOLHA DO GERENTE
     @Query(value="select id_gerente from conta  group by id_gerente order by count(id_gerente) asc limit 1",nativeQuery=true)
     public List<Long> idGerenteMenosClientes();

     @Query(value="select id_gerente from conta  group by id_gerente order by count(id_gerente) desc limit 1",nativeQuery=true)
     public List<Long> idGerenteMaisClientes();
     @Query(value="select id_gerente from conta where id_gerente != ?   group by id_gerente order by count(id_gerente) asc limit 1",nativeQuery=true)
     public List<Long> idGerenteMenosClientesMenosAtual(Long id_gerente);
   

     public List<ContaCUD> findByIdGerente(Long userId);

     @Query(value="delete from conta where id_usuario = ?1 ",nativeQuery=true)
     @Modifying
     public void excluirPorCliente(Long id_usuario);


     @Query(value="update conta set id_gerente = ?1  where id_conta = ?2",nativeQuery=true)
     @Modifying
     public void updateGerenteConta(Long id_gerente, Long id);

     
 }
     
     

