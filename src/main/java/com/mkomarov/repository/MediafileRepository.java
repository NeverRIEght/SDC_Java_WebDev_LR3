package com.mkomarov.repository;

import com.mkomarov.data.DatabaseProvider;
import com.mkomarov.entity.MediafileEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MediafileRepository extends AbstractRepository<MediafileEntity> {

    public MediafileRepository(String tableName) {
        super(tableName);
    }

    @Override
    public List<MediafileEntity> getAll() {
        List<MediafileEntity> result = new ArrayList<>();
        String sql = "SELECT id, filename, hash FROM " + tableName;
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MediafileEntity contact = mapRow(rs);
                result.add(contact);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all entities", e);
        }
        return result;
    }

    public List<MediafileEntity> getAllByOwnerId(Long ownerId) {
        List<MediafileEntity> result = new ArrayList<>();
        String sql = "SELECT m.id, m.filename, m.hash " +
                "FROM " + tableName + "as m " +
                "JOIN contacts as c ON c.mediafile_id = m.id " +
                "WHERE c.user_id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MediafileEntity contact = mapRow(rs);
                    result.add(contact);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all entities", e);
        }
        return result;
    }

    public List<MediafileEntity> getAllByContactId(Long contactId) {
        List<MediafileEntity> result = new ArrayList<>();
        String sql = "SELECT m.id, m.filename, m.hash " +
                "FROM " + tableName + "as m " +
                "JOIN contacts as c ON c.mediafile_id = m.id " +
                "WHERE c.id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, contactId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MediafileEntity contact = mapRow(rs);
                    result.add(contact);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all entities", e);
        }
        return result;
    }

    @Override
    public Optional<MediafileEntity> getById(long id) {
        String sql = "SELECT id, filename, hash FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch entity by id", e);
        }
        return Optional.empty();
    }

    @Override
    public MediafileEntity create(MediafileEntity entityToCreate) {
        String sql = "INSERT INTO " + tableName + " (filename, hash) VALUES (?, ?)";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entityToCreate.getFileName());
            ps.setString(2, entityToCreate.getHash());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Creating failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entityToCreate.setId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Creating failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create entity", e);
        }
        return entityToCreate;
    }

    @Override
    public MediafileEntity update(MediafileEntity updatedEntity) {
        String sql = "UPDATE " + tableName + " SET filename = ?, hash = ? WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedEntity.getFileName());
            ps.setString(2, updatedEntity.getHash());
            ps.setLong(3, updatedEntity.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Updating failed, no rows affected.");
            }
            return updatedEntity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    @Override
    public MediafileEntity delete(long id) {
        Optional<MediafileEntity> existing = getById(id);
        if (existing.isEmpty()) return null;
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) return null;
            return existing.get();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete entity", e);
        }
    }

    protected MediafileEntity mapRow(ResultSet rs) throws SQLException {
        return MediafileEntity.builder()
                .id(rs.getLong("id"))
                .fileName(rs.getString("filename"))
                .hash(rs.getString("hash"))
                .build();
    }
}
