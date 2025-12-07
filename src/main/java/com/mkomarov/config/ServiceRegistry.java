package com.mkomarov.config;

import com.mkomarov.service.ContactService;
import com.mkomarov.service.MediafileService;
import com.mkomarov.service.ObjectStorageService;
import com.mkomarov.service.UserService;

public final class ServiceRegistry {
    public static final MediafileService MEDIAFILE_SERVICE;
    public static final ContactService CONTACT_SERVICE;
    public static final ObjectStorageService OBJECT_STORAGE_SERVICE;
    public static final UserService USER_SERVICE;

    static {
        OBJECT_STORAGE_SERVICE = new ObjectStorageService();
        USER_SERVICE = new UserService();
        MEDIAFILE_SERVICE = new MediafileService(USER_SERVICE, OBJECT_STORAGE_SERVICE);
        CONTACT_SERVICE = new ContactService(USER_SERVICE);

        MEDIAFILE_SERVICE.setContactService(CONTACT_SERVICE);
        CONTACT_SERVICE.setMediafileService(MEDIAFILE_SERVICE);
    }

    private ServiceRegistry() {
    }
}

