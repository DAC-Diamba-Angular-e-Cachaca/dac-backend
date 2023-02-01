package com.bantads.autenticacao.bantadsautenticacao.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuario")
public class Usuario implements Serializable {
    @Id private String _id;
    private String email;
    private String senha;
    private String cargo;
    private boolean ativo;
}