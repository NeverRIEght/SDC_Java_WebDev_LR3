package com.mkomarov.entity;

import java.util.Objects;

public class MediafileEntity extends AbstractEntity {
    private UserEntity owner;
    private ContactEntity associatedContact;
    private String fileName;
    private String hash;

    public MediafileEntity() {
    }

    public MediafileEntity(UserEntity owner, ContactEntity associatedContact, String fileName, String hash) {
        this.owner = owner;
        this.associatedContact = associatedContact;
        this.fileName = fileName;
        this.hash = hash;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public ContactEntity getAssociatedContact() {
        return associatedContact;
    }

    public void setAssociatedContact(ContactEntity associatedContact) {
        this.associatedContact = associatedContact;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MediafileEntity entity = (MediafileEntity) o;
        return Objects.equals(owner, entity.owner) && Objects.equals(associatedContact, entity.associatedContact) && Objects.equals(fileName, entity.fileName) && Objects.equals(hash, entity.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, associatedContact, fileName, hash);
    }

    @Override
    public String toString() {
        return "MediafileEntity{" +
                "owner=" + owner +
                ", associatedContact=" + associatedContact +
                ", fileName='" + fileName + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
