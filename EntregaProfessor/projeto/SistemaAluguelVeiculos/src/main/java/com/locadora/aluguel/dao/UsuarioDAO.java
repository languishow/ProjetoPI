package com.locadora.aluguel.dao;

import com.locadora.aluguel.model.Usuario;
import com.locadora.aluguel.util.Conexao;

import java.sql.*;
import java.util.ArrayList;

public class UsuarioDAO {

    public Usuario autenticar(String login, String senha) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE login = ? AND senha = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, senha);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public void inserir(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (nome, cargo, login, senha, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getNome());
            ps.setString(2, u.getCargo());
            ps.setString(3, u.getLogin());
            ps.setString(4, u.getSenha());
            ps.setString(5, u.getEmail());
            ps.executeUpdate();
        }
    }

    public ArrayList<Usuario> buscarTodos() throws SQLException {
        ArrayList<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY nome";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("cargo"),
            rs.getString("login"),
            rs.getString("senha"),
            rs.getString("email")
        );
    }
}
