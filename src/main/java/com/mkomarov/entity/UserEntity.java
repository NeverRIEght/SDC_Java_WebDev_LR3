package com.mkomarov.entity;

import java.util.Objects;

public class UserEntity extends AbstractEntity {
    private String email;
    private String passwordHash;

    public UserEntity() {
    }

    public UserEntity(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(email, that.email) && Objects.equals(passwordHash, that.passwordHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, passwordHash);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                '}';
    }
}
