package com.mkomarov.service;

import com.mkomarov.dto.ContactDto;
import com.mkomarov.entity.ContactEntity;
import com.mkomarov.entity.UserEntity;
import com.mkomarov.repository.ContactRepository;

import java.util.List;
import java.util.Optional;

public class ContactService {
    private final ContactRepository contactRepository;
    private final UserService userService;

    public ContactService() {
        this.contactRepository = new ContactRepository("contacts");
        this.userService = new UserService();
    }

    public Optional<ContactEntity> getContactById(long id) {
        return contactRepository.getById(id);
    }

    public void createContact (ContactDto request) {
        Optional<UserEntity> owner = userService.getUserByEmail(request.getOwnerEmail());

        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + request.getOwnerEmail() + " does not exist.");
        }

        validateContactDto(request);

        ContactEntity newContact = new ContactEntity(
                owner.get(),
                request.getName(),
                request.getSurname(),
                request.getPhoneNumber()
        );

        contactRepository.create(newContact);
    }

    public void updateContact(ContactDto request) {
        Optional<UserEntity> owner = userService.getUserByEmail(request.getOwnerEmail());

        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + request.getOwnerEmail() + " does not exist.");
        }

        if (request.getId() == null) {
            throw new IllegalArgumentException("Contact ID cannot be null.");
        }

        Optional<ContactEntity> existingContact = contactRepository.getById(request.getId());

        validateContactDto(request);

        existingContact.ifPresent(contactEntity -> {
            contactEntity.setOwner(owner.get());
            contactEntity.setName(request.getName());
            contactEntity.setSurname(request.getSurname());
            contactEntity.setPhoneNumber(request.getPhoneNumber());

            contactRepository.update(contactEntity);
        });
    }

    public void deleteContact(long id) {
        contactRepository.delete(id);
    }

    private void validateContactDto(ContactDto request) {
        String name = request.getName();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Contact name cannot be null or empty.");
        }

        String surname = request.getSurname();
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Contact surname cannot be null or empty.");
        }

        String phoneNumber = request.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Contact phone number cannot be null or empty.");
        }
    }

    public List<ContactEntity> getAllContacts(String userEmail) {
        Optional<UserEntity> owner = userService.getUserByEmail(userEmail);
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + userEmail + " does not exist.");
        }
        return contactRepository.getAllByOwnerId(owner.get().getId());
    }
}
