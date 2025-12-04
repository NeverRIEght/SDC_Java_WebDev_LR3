package com.mkomarov.config.config;

public final class ObjectStorageProperties {
    public static final String REGION = System.getenv("OBJECT_STORAGE_REGION");
    public static final String INTERNAL_ENDPOINT = System.getenv("OBJECT_STORAGE_INTERNAL_ENDPOINT");
    public static final String PUBLIC_ENDPOINT = System.getenv("OBJECT_STORAGE_PUBLIC_ENDPOINT");
    public static final String ACCESS_KEY = System.getenv("OBJECT_STORAGE_ACCESS_KEY");
    public static final String SECRET_KEY = System.getenv("OBJECT_STORAGE_SECRET_KEY");
}
