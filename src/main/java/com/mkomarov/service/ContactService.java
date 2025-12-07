package com.mkomarov.service;

import com.mkomarov.dto.ContactDto;
import com.mkomarov.entity.ContactEntity;
import com.mkomarov.entity.MediafileEntity;
import com.mkomarov.entity.UserEntity;
import com.mkomarov.repository.ContactRepository;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ContactService {
    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;

    private final UserService userService;
    @Setter
    private MediafileService mediafileService;

    public ContactService(UserService userService) {
        this.contactRepository = new ContactRepository("contacts");
        this.userService = userService;
    }

    public List<ContactEntity> getAllContacts(String userEmail) {
        Optional<UserEntity> owner = userService.getUserByEmail(userEmail);
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + userEmail + " does not exist.");
        }
        return contactRepository.getAllByUserId(owner.get().getId());
    }

    public Optional<ContactEntity> getContactById(String userEmail, long id) {
        Optional<UserEntity> owner = userService.getUserByEmail(userEmail);
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + userEmail + " does not exist.");
        }
        Optional<ContactEntity> foundEntity = contactRepository.getById(id);
        if (foundEntity.isPresent() && foundEntity.get().getUserId() != owner.get().getId()) {
            log.warn("Invalid access attempt by user {} to contact with id {}, returning empty entity.", userEmail, id);
            return Optional.empty();
        }
        return foundEntity;
    }

    public void createContact(ContactDto request) {
        Optional<UserEntity> owner = userService.getUserByEmail(request.getOwnerEmail());
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + request.getOwnerEmail() + " does not exist.");
        }

        validateContactDto(request);

        ContactEntity newContact = ContactEntity.builder()
                .userId(owner.get().getId())
                .name(request.getName())
                .surname(request.getSurname())
                .phoneNumber(request.getPhoneNumber())
                .build();

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
        if (existingContact.isEmpty() || existingContact.get().getUserId() != owner.get().getId()) {
            throw new IllegalArgumentException("Contact with ID " + request.getId() + " does not exist.");
        }

        validateContactDto(request);

        Long mediafileId = request.getAssociatedMediafileId();
        Optional<MediafileEntity> mediafileOpt = mediafileService.getById(mediafileId);
        if (mediafileOpt.isEmpty()) {
            throw new IllegalArgumentException("Mediafile with ID " + mediafileId + " does not exist.");
        }

        ContactEntity updatedContact = ContactEntity.builder()
                .id(request.getId())
                .userId(owner.get().getId())
                .name(request.getName())
                .surname(request.getSurname())
                .phoneNumber(request.getPhoneNumber())
                .mediafileId(request.getAssociatedMediafileId())
                .build();


        contactRepository.update(updatedContact);
    }

    public void addMediafileToContact(long mediafileId, long contactId) {
        Optional<ContactEntity> contactOpt = contactRepository.getById(contactId);
        if (contactOpt.isEmpty()) {
            throw new IllegalArgumentException("Contact with ID " + contactId + " does not exist.");
        }
        ContactEntity contact = contactOpt.get();

        Optional<com.mkomarov.entity.MediafileEntity> mediafileOpt = mediafileService.getById(mediafileId);
        if (mediafileOpt.isEmpty()) {
            throw new IllegalArgumentException("Mediafile with ID " + mediafileId + " does not exist.");
        }

        contact.setMediafileId(mediafileId);
        contactRepository.update(contact);
    }

    public void removeMediafileFromContact(long contactId) {
        Optional<ContactEntity> contactOpt = contactRepository.getById(contactId);
        if (contactOpt.isEmpty()) {
            throw new IllegalArgumentException("Contact with ID " + contactId + " does not exist.");
        }
        ContactEntity contact = contactOpt.get();

        contact.setMediafileId(null);
        contactRepository.update(contact);
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
        if (!phoneNumber.matches("^\\+?\\d{11}$")) {
            throw new IllegalArgumentException("Contact phone number format is invalid.");
        }
    }
}
