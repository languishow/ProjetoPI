package com.locadora.aluguel.dao;

import com.locadora.aluguel.model.Veiculo;
import com.locadora.aluguel.util.Conexao;

import java.sql.*;
import java.util.ArrayList;

public class VeiculoDAO {

    public void inserir(Veiculo v) throws SQLException {
        String sql = "INSERT INTO veiculo (numero, placa, fabricante, modelo, anoModelo, qtdPortas, acessorios) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getNumero());
            ps.setString(2, v.getPlaca());
            ps.setString(3, v.getFabricante());
            ps.setString(4, v.getModelo());
            ps.setInt(5, v.getAnoModelo());
            ps.setInt(6, v.getQtdPortas());
            ps.setString(7, v.getAcessorios());
            ps.executeUpdate();
        }
    }

    public void atualizar(Veiculo v) throws SQLException {
        String sql = "UPDATE veiculo SET placa=?, fabricante=?, modelo=?, anoModelo=?, qtdPortas=?, acessorios=? WHERE numero=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getFabricante());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAnoModelo());
            ps.setInt(5, v.getQtdPortas());
            ps.setString(6, v.getAcessorios());
            ps.setString(7, v.getNumero());
            ps.executeUpdate();
        }
    }

    public void excluir(String numero) throws SQLException {
        String sql = "DELETE FROM veiculo WHERE numero=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);
            ps.executeUpdate();
        }
    }

    public ArrayList<Veiculo> buscarTodos() throws SQLException {
        ArrayList<Veiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM veiculo ORDER BY placa";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Veiculo buscarPorNumero(String numero) throws SQLException {
        String sql = "SELECT * FROM veiculo WHERE numero=?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public boolean veiculoDisponivel(String numero) throws SQLException {
        String sql = "SELECT COUNT(*) FROM aluguel WHERE numero_veiculo=? AND entregue='N'";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    private Veiculo mapear(ResultSet rs) throws SQLException {
        return new Veiculo(
            rs.getString("numero"),
            rs.getString("placa"),
            rs.getString("fabricante"),
            rs.getString("modelo"),
            rs.getInt("anoModelo"),
            rs.getInt("qtdPortas"),
            rs.getString("acessorios")
        );
    }
}
