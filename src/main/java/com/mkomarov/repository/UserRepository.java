package com.mkomarov.repository;

import com.mkomarov.db.DatabaseProvider;
import com.mkomarov.entity.UserEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserRepository extends AbstractRepository<UserEntity> {

    public UserRepository(String tableName) {
        super(tableName);
    }

    @Override
    public List<UserEntity> getAll() {
        List<UserEntity> result = new ArrayList<>();
        String sql = "SELECT id, email, password_hash FROM " + tableName;
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UserEntity user = mapRow(rs);
                result.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all users", e);
        }
        return result;
    }

    @Override
    public Optional<UserEntity> getById(long id) {
        String sql = "SELECT id, email, password_hash FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user by id", e);
        }
        return Optional.empty();
    }

    @Override
    public UserEntity create(UserEntity entityToCreate) {
        String sql = "INSERT INTO " + tableName + " (email, password_hash) VALUES (?, ?)";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entityToCreate.getEmail());
            ps.setString(2, entityToCreate.getPasswordHash());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Creating user failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entityToCreate.setId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
        return entityToCreate;
    }

    @Override
    public UserEntity update(UserEntity updatedEntity) {
        String sql = "UPDATE " + tableName + " SET email = ?, password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedEntity.getEmail());
            ps.setString(2, updatedEntity.getPasswordHash());
            ps.setLong(3, updatedEntity.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Updating user failed, no rows affected.");
            }
            return updatedEntity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public UserEntity delete(long id) {
        Optional<UserEntity> existing = getById(id);
        if (existing.isEmpty()) return null;
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) return null;
            return existing.get();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    public Optional<UserEntity> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = "SELECT id, email, password_hash FROM " + tableName + " WHERE email = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
        return Optional.empty();
    }

    private UserEntity mapRow(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    }
}
