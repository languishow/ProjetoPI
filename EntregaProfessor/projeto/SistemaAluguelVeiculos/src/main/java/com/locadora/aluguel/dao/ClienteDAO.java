package com.locadora.aluguel.dao;

import com.locadora.aluguel.model.Cliente;
import com.locadora.aluguel.util.Conexao;

import java.sql.*;
import java.util.ArrayList;

public class ClienteDAO {

    public void inserir(Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (cpf, nomeCliente, endereco, uf, telefone, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getCpf());
            ps.setString(2, c.getNomeCliente());
            ps.setString(3, c.getEndereco());
            ps.setString(4, c.getUf());
            ps.setString(5, c.getTelefone());
            ps.setString(6, c.getEmail());
            ps.executeUpdate();
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nomeCliente=?, endereco=?, uf=?, telefone=?, email=? WHERE cpf=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNomeCliente());
            ps.setString(2, c.getEndereco());
            ps.setString(3, c.getUf());
            ps.setString(4, c.getTelefone());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getCpf());
            ps.executeUpdate();
        }
    }

    public void excluir(String cpf) throws SQLException {
        String sql = "DELETE FROM cliente WHERE cpf=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cpf);
            ps.executeUpdate();
        }
    }

    public ArrayList<Cliente> buscarTodos() throws SQLException {
        ArrayList<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY nomeCliente";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Cliente buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE cpf=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getString("cpf"),
            rs.getString("nomeCliente"),
            rs.getString("endereco"),
            rs.getString("uf"),
            rs.getString("telefone"),
            rs.getString("email")
        );
    }
}
