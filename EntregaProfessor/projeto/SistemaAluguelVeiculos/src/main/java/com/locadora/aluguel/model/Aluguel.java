package com.locadora.aluguel.model;

import java.sql.Date;

public class Aluguel {

    private int     idaluguel;
    private Veiculo veiculo;
    private Cliente cliente;
    private Date    dataAluguel;
    private Date    dataentrega;
    private char    entregue;
    private String  observacao;
    private double  valorPago;

    public Aluguel() {
        this.entregue = 'N';
    }

    public Aluguel(int idaluguel, Veiculo veiculo, Cliente cliente,
                   Date dataAluguel, Date dataentrega, char entregue,
                   String observacao, double valorPago) {
        this.idaluguel   = idaluguel;
        this.veiculo     = veiculo;
        this.cliente     = cliente;
        this.dataAluguel = dataAluguel;
        this.dataentrega = dataentrega;
        this.entregue    = entregue;
        this.observacao  = observacao;
        this.valorPago   = valorPago;
    }

    public int getIdaluguel()                { return idaluguel; }
    public void setIdaluguel(int idaluguel)  { this.idaluguel = idaluguel; }

    public Veiculo getVeiculo()              { return veiculo; }
    public void setVeiculo(Veiculo veiculo)  { this.veiculo = veiculo; }

    public Cliente getCliente()              { return cliente; }
    public void setCliente(Cliente cliente)  { this.cliente = cliente; }

    public Date getDataAluguel()                   { return dataAluguel; }
    public void setDataAluguel(Date dataAluguel)   { this.dataAluguel = dataAluguel; }

    public Date getDataentrega()                   { return dataentrega; }
    public void setDataentrega(Date dataentrega)   { this.dataentrega = dataentrega; }

    public char getEntregue()              { return entregue; }
    public void setEntregue(char entregue) { this.entregue = entregue; }

    public String getObservacao()                  { return observacao; }
    public void setObservacao(String observacao)   { this.observacao = observacao; }

    public double getValorPago()               { return valorPago; }
    public void setValorPago(double valorPago)  { this.valorPago = valorPago; }
}
