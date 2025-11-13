package com.mkomarov.repository;

import com.mkomarov.db.DatabaseProvider;
import com.mkomarov.entity.AbstractEntity;

import java.util.List;

public abstract class AbstractRepository<T extends AbstractEntity> {
    protected String tableName;
    protected DatabaseProvider dbInstance;

    public AbstractRepository(String tableName, DatabaseProvider dbInstance) {
        this.tableName = tableName;
        this.dbInstance = dbInstance;
    }

    protected abstract List<T> getAll();
    protected abstract T getById(long id);
    protected abstract T create(T entityToCreate);
    protected abstract T update(long id, T updatedEntity);
    protected abstract T delete(long id);
}
