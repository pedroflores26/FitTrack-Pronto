package br.ulbra.dao;

import br.ulbra.model.Usuario;
import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioDAO {

    // --- SALVAR USUÁRIO ---
    public void salvar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (nome, idade, peso, senha, altura) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = AbstractDAO.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Aqui a senha já vem hashada pelo CadastroUsu
            ps.setString(1, u.getNome());
            ps.setInt(2, u.getIdade());
            ps.setDouble(3, u.getPeso());
            ps.setString(4, u.getSenha());
            ps.setDouble(5, u.getAltura());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Falha ao inserir usuário.");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }
        }
    }

    // --- AUTENTICAR ---
   public Usuario autenticar(String nome, String senhaDigitada) throws SQLException {
    String sql = "SELECT * FROM usuario WHERE TRIM(nome) = ?";
    try (Connection con = AbstractDAO.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, nome.trim());
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) return null;

        String senhaBanco = rs.getString("senha").trim();
        Usuario u = new Usuario(
            rs.getInt("id_usuario"),
            rs.getString("nome"),
            senhaBanco,
            rs.getInt("idade"),
            rs.getDouble("peso"),
            rs.getDouble("altura")
        );

        // Verifica senha com BCrypt
        if (senhaBanco.startsWith("$2a$") || senhaBanco.startsWith("$2b$")) {
            if (BCrypt.checkpw(senhaDigitada, senhaBanco)) return u;
            else return null;
        } else {
            // senha antiga em texto puro
            if (senhaDigitada.equals(senhaBanco)) {
                String novoHash = BCrypt.hashpw(senhaDigitada, BCrypt.gensalt());
                try (PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE usuario SET senha=? WHERE id_usuario=?")) {
                    ps2.setString(1, novoHash);
                    ps2.setInt(2, u.getId());
                    ps2.executeUpdate();
                }
                u.setSenha(novoHash);
                return u;
            } else {
                return null;
            }
        }
    }
}

    // --- BUSCAR POR NOME ---
    public Usuario buscarPorNome(String nome) {
        String sql = "SELECT * FROM usuario WHERE nome = ?";
        Usuario u = null;
        try (Connection con = AbstractDAO.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        rs.getInt("idade"),
                        rs.getDouble("peso"),
                        rs.getDouble("altura")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return u;
    }

    // --- LISTAR ---
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id_usuario";
        try (Connection con = AbstractDAO.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nome"),
                    rs.getString("senha"),
                    rs.getInt("idade"),
                    rs.getDouble("peso"),
                    rs.getDouble("altura")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // --- ATUALIZAR ---
    public void atualizar(Usuario u) {
        String sql = "UPDATE usuario SET nome=?, senha=?, idade=?, peso=?, altura=? WHERE id_usuario=?";
        try (Connection con = AbstractDAO.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // senha já deve estar hashada no objeto Usuario
            ps.setString(1, u.getNome());
            ps.setString(2, u.getSenha());
            ps.setInt(3, u.getIdade());
            ps.setDouble(4, u.getPeso());
            ps.setDouble(5, u.getAltura());
            ps.setInt(6, u.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- REMOVER ---
    public void remover(int id) {
        String sql = "DELETE FROM usuario WHERE id_usuario=?";
        try (Connection con = AbstractDAO.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // --- BUSCAR POR ID ---
public Usuario buscarPorId(int id) {
    String sql = "SELECT * FROM usuario WHERE id_usuario = ?";
    Usuario u = null;
    try (Connection con = AbstractDAO.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                u = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        rs.getInt("idade"),
                        rs.getDouble("peso"),
                        rs.getDouble("altura")
                );
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return u;
}

}

