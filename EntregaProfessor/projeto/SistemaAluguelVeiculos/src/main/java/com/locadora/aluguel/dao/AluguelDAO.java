package com.locadora.aluguel.dao;

import com.locadora.aluguel.model.Aluguel;
import com.locadora.aluguel.model.Cliente;
import com.locadora.aluguel.model.Veiculo;
import com.locadora.aluguel.util.Conexao;

import java.sql.*;
import java.util.ArrayList;

public class AluguelDAO {

    public void inserir(Aluguel a) throws SQLException {
        VeiculoDAO veiculoDAO = new VeiculoDAO();
        if (!veiculoDAO.veiculoDisponivel(a.getVeiculo().getNumero())) {
            throw new SQLException("Veículo indisponível: já possui aluguel em aberto.");
        }

        String sql = "INSERT INTO aluguel (numero_veiculo, cpf_cliente, dataAluguel, dataentrega, entregue, observacao, valorPago) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getVeiculo().getNumero());
            ps.setString(2, a.getCliente().getCpf());
            ps.setDate(3, a.getDataAluguel());
            ps.setDate(4, a.getDataentrega());
            ps.setString(5, String.valueOf(a.getEntregue()));
            ps.setString(6, a.getObservacao());
            ps.setDouble(7, a.getValorPago());
            ps.executeUpdate();
        }
    }

    public void atualizar(Aluguel a) throws SQLException {
        String sql = "UPDATE aluguel SET numero_veiculo=?, cpf_cliente=?, dataAluguel=?, dataentrega=?, "
                   + "entregue=?, observacao=?, valorPago=? WHERE idaluguel=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getVeiculo().getNumero());
            ps.setString(2, a.getCliente().getCpf());
            ps.setDate(3, a.getDataAluguel());
            ps.setDate(4, a.getDataentrega());
            ps.setString(5, String.valueOf(a.getEntregue()));
            ps.setString(6, a.getObservacao());
            ps.setDouble(7, a.getValorPago());
            ps.setInt(8, a.getIdaluguel());
            ps.executeUpdate();
        }
    }

    public void marcarEntregue(int id) throws SQLException {
        String sql = "UPDATE aluguel SET entregue='S' WHERE idaluguel=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM aluguel WHERE idaluguel=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public ArrayList<Aluguel> buscarTodos() throws SQLException {
        String sql = "SELECT a.idaluguel, a.dataAluguel, a.dataentrega, a.entregue, a.observacao, a.valorPago, "
                   + "v.numero, v.placa, v.fabricante, v.modelo, v.anoModelo, v.qtdPortas, v.acessorios, "
                   + "c.cpf, c.nomeCliente, c.endereco, c.uf, c.telefone, c.email "
                   + "FROM aluguel a "
                   + "JOIN veiculo v ON a.numero_veiculo = v.numero "
                   + "JOIN cliente c ON a.cpf_cliente = c.cpf "
                   + "ORDER BY a.dataAluguel DESC";
        return executarConsulta(sql);
    }

    public ArrayList<Aluguel> buscarPorPeriodo(Date inicio, Date fim) throws SQLException {
        String sql = "SELECT a.idaluguel, a.dataAluguel, a.dataentrega, a.entregue, a.observacao, a.valorPago, "
                   + "v.numero, v.placa, v.fabricante, v.modelo, v.anoModelo, v.qtdPortas, v.acessorios, "
                   + "c.cpf, c.nomeCliente, c.endereco, c.uf, c.telefone, c.email "
                   + "FROM aluguel a "
                   + "JOIN veiculo v ON a.numero_veiculo = v.numero "
                   + "JOIN cliente c ON a.cpf_cliente = c.cpf "
                   + "WHERE a.dataAluguel BETWEEN ? AND ? "
                   + "ORDER BY a.dataAluguel DESC";

        ArrayList<Aluguel> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, inicio);
            ps.setDate(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public ArrayList<Aluguel> buscarAtrasados() throws SQLException {
        String sql = "SELECT a.idaluguel, a.dataAluguel, a.dataentrega, a.entregue, a.observacao, a.valorPago, "
                   + "v.numero, v.placa, v.fabricante, v.modelo, v.anoModelo, v.qtdPortas, v.acessorios, "
                   + "c.cpf, c.nomeCliente, c.endereco, c.uf, c.telefone, c.email "
                   + "FROM aluguel a "
                   + "JOIN veiculo v ON a.numero_veiculo = v.numero "
                   + "JOIN cliente c ON a.cpf_cliente = c.cpf "
                   + "WHERE a.entregue='N' AND a.dataentrega < CURDATE() "
                   + "ORDER BY a.dataentrega";
        return executarConsulta(sql);
    }

    public double calcularFaturamento(Date inicio, Date fim) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valorPago), 0) FROM aluguel WHERE dataAluguel BETWEEN ? AND ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, inicio);
            ps.setDate(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    private ArrayList<Aluguel> executarConsulta(String sql) throws SQLException {
        ArrayList<Aluguel> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Aluguel mapear(ResultSet rs) throws SQLException {
        Veiculo veiculo = new Veiculo(
            rs.getString("numero"),
            rs.getString("placa"),
            rs.getString("fabricante"),
            rs.getString("modelo"),
            rs.getInt("anoModelo"),
            rs.getInt("qtdPortas"),
            rs.getString("acessorios")
        );

        Cliente cliente = new Cliente(
            rs.getString("cpf"),
            rs.getString("nomeCliente"),
            rs.getString("endereco"),
            rs.getString("uf"),
            rs.getString("telefone"),
            rs.getString("email")
        );

        String entregueStr = rs.getString("entregue");
        char entregue = (entregueStr != null && !entregueStr.isEmpty()) ? entregueStr.charAt(0) : 'N';

        return new Aluguel(
            rs.getInt("idaluguel"),
            veiculo,
            cliente,
            rs.getDate("dataAluguel"),
            rs.getDate("dataentrega"),
            entregue,
            rs.getString("observacao"),
            rs.getDouble("valorPago")
        );
    }
}
