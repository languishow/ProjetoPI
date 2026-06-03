package com.locadora.aluguel.model;

public class Cliente {

    private String cpf;
    private String nomeCliente;
    private String endereco;
    private String uf;
    private String telefone;
    private String email;

    public Cliente() {}

    public Cliente(String cpf, String nomeCliente, String endereco, String uf, String telefone, String email) {
        this.cpf         = cpf;
        this.nomeCliente = nomeCliente;
        this.endereco    = endereco;
        this.uf          = uf;
        this.telefone    = telefone;
        this.email       = email;
    }

    public String getCpf()               { return cpf; }
    public void setCpf(String cpf)       { this.cpf = cpf; }

    public String getNomeCliente()                   { return nomeCliente; }
    public void setNomeCliente(String nomeCliente)   { this.nomeCliente = nomeCliente; }

    public String getEndereco()                { return endereco; }
    public void setEndereco(String endereco)   { this.endereco = endereco; }

    public String getUf()              { return uf; }
    public void setUf(String uf)       { this.uf = uf; }

    public String getTelefone()                { return telefone; }
    public void setTelefone(String telefone)   { this.telefone = telefone; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

    @Override
    public String toString() {
        return nomeCliente;
    }
}
