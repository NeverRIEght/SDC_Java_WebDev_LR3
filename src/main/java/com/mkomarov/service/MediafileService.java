package com.mkomarov.service;

import com.mkomarov.dto.MediafileDto;
import com.mkomarov.entity.ContactEntity;
import com.mkomarov.entity.MediafileEntity;
import com.mkomarov.entity.UserEntity;
import com.mkomarov.repository.MediafileRepository;
import com.mkomarov.utils.HashingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MediafileService {
    private static final Logger log = LoggerFactory.getLogger(MediafileService.class);

    private final ObjectStorageService objectStorageService;

    private final MediafileRepository mediafileRepository;
    private final UserService userService;
    private final ContactService contactService;

    public MediafileService() {
        this.mediafileRepository = new MediafileRepository("mediafiles");
        this.userService = new UserService();
        this.contactService = new ContactService();
        this.objectStorageService = new ObjectStorageService();
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

        Optional<ContactEntity> associatedContact = contactService.getContactById(associatedContactId);
        if (associatedContact.isEmpty()) {
            throw new IllegalArgumentException("Invalid associated contact with ID: " + associatedContactId);
        }

        if (!(associatedContact.get().getOwner().getId() == owner.get().getId())) {
            throw new IllegalArgumentException("Invalid associated contact with ID: " + associatedContactId);
        }

        MediafileEntity mediafileEntity = new MediafileEntity();
        mediafileEntity.setFileName(request.getFilename());
        mediafileEntity.setOwner(owner.get());
        mediafileEntity.setAssociatedContact(associatedContact.get());

        String hash;
        try (ByteArrayInputStream hashStream = new ByteArrayInputStream(request.getFileData())){
            hash = HashingService.calculateSha256(hashStream);
        } catch (IOException e) {
            log.error("Error calculating hash for mediafile: {}", e.getMessage());
            throw new RuntimeException("Error processing mediafile data.");
        }

        mediafileEntity.setHash(hash);
        MediafileEntity entity = mediafileRepository.create(mediafileEntity);

        long id = entity.getId();
        request.setId(id);

        try {
            objectStorageService.uploadMediaFile(request);
            log.info("Mediafile with id {} uploaded to object storage.", id);
        } catch (Exception e) {
            log.error("Error uploading mediafile to object storage: {}", e.getMessage());
            mediafileRepository.delete(id);
            throw new RuntimeException("Error uploading mediafile.");
        }
    }

    public void deleteById(long id) {
        mediafileRepository.delete(id);
    }
}
