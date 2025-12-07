package com.mkomarov.service;

import com.mkomarov.dto.MediafileDto;
import com.mkomarov.entity.ContactEntity;
import com.mkomarov.entity.MediafileEntity;
import com.mkomarov.entity.UserEntity;
import com.mkomarov.repository.MediafileRepository;
import com.mkomarov.utils.HashingService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MediafileService {
    private static final Logger log = LoggerFactory.getLogger(MediafileService.class);

    private final MediafileRepository mediafileRepository;

    private final UserService userService;
    @Setter
    private ContactService contactService;
    private final ObjectStorageService objectStorageService;

    public MediafileService(UserService userService, ObjectStorageService objectStorageService) {
        this.mediafileRepository = new MediafileRepository("mediafiles");
        this.userService = userService;
        this.objectStorageService = objectStorageService;
    }

    public List<MediafileEntity> getAllByUserEmail(String userEmail) {
        Optional<UserEntity> owner = userService.getUserByEmail(userEmail);
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + userEmail + " does not exist.");
        }

        return mediafileRepository.getAllByOwnerId(owner.get().getId());
    }

    public Optional<MediafileEntity> getById(long id) {
        return mediafileRepository.getById(id);
    }

    public void create(MediafileDto request) {
        Optional<UserEntity> owner = userService.getUserByEmail(request.getOwnerEmail());
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("Owner with email " + request.getOwnerEmail() + " does not exist.");
        }

        Long associatedContactId = request.getAssociatedContactId();
        if (associatedContactId == null) {
            throw new IllegalArgumentException("Associated contact ID must be provided.");
        }

        Optional<ContactEntity> associatedContact = contactService.getContactById(
                request.getOwnerEmail(),
                associatedContactId
        );

        if (associatedContact.isEmpty()) {
            throw new IllegalArgumentException("Invalid associated contact with ID: " + associatedContactId);
        }
        ContactEntity contact = associatedContact.get();

        if (!(contact.getUserId() == owner.get().getId())) {
            throw new IllegalArgumentException("Invalid associated contact with ID: " + associatedContactId);
        }

        MediafileEntity mediafileEntity = new MediafileEntity();
        mediafileEntity.setFileName(request.getFilename());

        try (ByteArrayInputStream hashStream = new ByteArrayInputStream(request.getFileData())) {
            String hash = HashingService.calculateSha256(hashStream);
            mediafileEntity.setHash(hash);
        } catch (IOException e) {
            log.error("Error calculating hash for mediafile: {}", e.getMessage());
            throw new RuntimeException("Error processing mediafile data.");
        }

        mediafileEntity = mediafileRepository.create(mediafileEntity);
        long mediafileId = mediafileEntity.getId();
        request.setId(mediafileId);


        try {
            contactService.addMediafileToContact(mediafileId, contact.getId());
        } catch (IllegalArgumentException e) {
            log.error("Error associating mediafile with contact: {}", e.getMessage());
            log.info("Rolling back mediafile creation with id {}.", mediafileId);
            mediafileRepository.delete(mediafileId);
            throw new RuntimeException("Error associating mediafile with contact.");
        }

        try {
            objectStorageService.uploadMediaFile(request);
            log.info("Mediafile with id {} uploaded to object storage.", mediafileId);
        } catch (Exception e) {
            log.error("Error uploading mediafile to object storage: {}", e.getMessage());
            log.info("Rolling back mediafile association with contact for mediafile id {}.", mediafileId);
            contactService.removeMediafileFromContact(contact.getId());
            log.info("Rolling back mediafile creation with id {}.", mediafileId);
            mediafileRepository.delete(mediafileId);
            throw new RuntimeException("Error uploading mediafile.");
        }
    }

    public void deleteById(long id) {
        mediafileRepository.delete(id);
    }
}
