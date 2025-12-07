package com.mkomarov.repository;

import com.mkomarov.data.DatabaseProvider;
import com.mkomarov.entity.ContactEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ContactRepository extends AbstractRepository<ContactEntity> {

    public ContactRepository(String tableName) {
        super(tableName);
    }

    @Override
    public List<ContactEntity> getAll() {
        List<ContactEntity> result = new ArrayList<>();
        String sql = "SELECT id, user_id, name, surname, phone_number, mediafile_id FROM " + tableName;
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ContactEntity contact = mapRow(rs);
                result.add(contact);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all contacts", e);
        }
        return result;
    }

    public List<ContactEntity> getAllByUserId(Long ownerId) {
        List<ContactEntity> result = new ArrayList<>();
        String sql = "SELECT id, user_id, name, surname, phone_number, mediafile_id FROM "
                + tableName + " WHERE user_id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContactEntity contact = mapRow(rs);
                    result.add(contact);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all contacts", e);
        }
        return result;
    }

    @Override
    public Optional<ContactEntity> getById(long id) {
        String sql = "SELECT id, user_id, name, surname, phone_number, mediafile_id FROM "
                + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch contact by id", e);
        }
        return Optional.empty();
    }

    @Override
    public ContactEntity create(ContactEntity entityToCreate) {
        String sql = "INSERT INTO " + tableName + " (user_id, name, surname, phone_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entityToCreate.getUserId());
            ps.setString(2, entityToCreate.getName());
            ps.setString(3, entityToCreate.getSurname());
            ps.setString(4, entityToCreate.getPhoneNumber());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Creating contact failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entityToCreate.setId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Creating contact failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create contact", e);
        }
        return entityToCreate;
    }

    @Override
    public ContactEntity update(ContactEntity updatedEntity) {
        String sql = "UPDATE " + tableName + " SET name = ?, surname = ?, phone_number = ?, mediafile_id = ? WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedEntity.getName());
            ps.setString(2, updatedEntity.getSurname());
            ps.setString(3, updatedEntity.getPhoneNumber());

            if (updatedEntity.getMediafileId() == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, updatedEntity.getMediafileId());
            }

            ps.setLong(5, updatedEntity.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Updating contact failed, no rows affected.");
            }
            return updatedEntity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update contact", e);
        }
    }

    @Override
    public ContactEntity delete(long id) {
        Optional<ContactEntity> existing = getById(id);
        if (existing.isEmpty()) return null;
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DatabaseProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) return null;
            return existing.get();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete contact", e);
        }
    }

    protected ContactEntity mapRow(ResultSet rs) throws SQLException {
        return ContactEntity.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .name(rs.getString("name"))
                .surname(rs.getString("surname"))
                .phoneNumber(rs.getString("phone_number"))
                .mediafileId(rs.getObject("mediafile_id", Long.class))
                .build();
    }
}
