package com.mkomarov.repository;

import com.mkomarov.entity.AbstractEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRepository<T extends AbstractEntity> {
    protected String tableName;

    public AbstractRepository(String tableName) {
        this.tableName = tableName;
    }

    protected abstract List<T> getAll();

    protected abstract Optional<T> getById(long id);

    protected abstract T create(T entityToCreate);

    protected abstract T update(T updatedEntity);

    protected abstract T delete(long id);

    protected abstract T mapRow(ResultSet rs) throws SQLException;
}
