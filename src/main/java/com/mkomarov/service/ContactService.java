package com.mkomarov.service;

import com.mkomarov.entity.ContactEntity;
import com.mkomarov.repository.ContactRepository;

import java.util.Optional;

public class ContactService {
    private final ContactRepository contactRepository;

    public ContactService() {
        this.contactRepository = new ContactRepository("contacts");
    }

    public Optional<ContactEntity> getContactById(long id) {
        return contactRepository.getById(id);
    }
}
