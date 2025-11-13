package com.mkomarov.repository;

import db.DatabaseProvider;
import entity.UserEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class UserRepository extends AbstractRepository<UserEntity> {

    public UserRepository(String tableName, DatabaseProvider dbInstance) {
        super(tableName, dbInstance);
    }

    @Override
    protected List<UserEntity> getAll() {
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
    protected UserEntity getById(long id) {
        String sql = "SELECT id, email, password_hash FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user by id", e);
        }
        return null;
    }

    @Override
    protected UserEntity create(UserEntity entityToCreate) {
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
    protected UserEntity update(long id, UserEntity updatedEntity) {
        String sql = "UPDATE " + tableName + " SET email = ?, password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedEntity.getEmail());
            ps.setString(2, updatedEntity.getPasswordHash());
            ps.setLong(3, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Updating user failed, no rows affected.");
            }
            updatedEntity.setId(id);
            return updatedEntity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    protected UserEntity delete(long id) {
        UserEntity existing = getById(id);
        if (existing == null) return null;
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) return null;
            return existing;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    private UserEntity mapRow(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    }
}

