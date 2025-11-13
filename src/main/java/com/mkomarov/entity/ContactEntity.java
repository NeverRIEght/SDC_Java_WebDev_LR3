package com.mkomarov.entity;

import java.util.Objects;

public class ContactEntity extends AbstractEntity {
    private UserEntity owner;
    private String name;
    private String surname;
    private String phoneNumber;

    public ContactEntity() {
    }

    public ContactEntity(UserEntity owner, String name, String surname, String phoneNumber) {
        this.owner = owner;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ContactEntity that = (ContactEntity) o;
        return Objects.equals(owner, that.owner) && Objects.equals(name, that.name) && Objects.equals(surname, that.surname) && Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, surname, phoneNumber);
    }

    @Override
    public String toString() {
        return "ContactEntity{" +
                "owner=" + owner +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
