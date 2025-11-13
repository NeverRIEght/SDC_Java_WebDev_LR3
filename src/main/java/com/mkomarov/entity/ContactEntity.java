package com.mkomarov.entity;

import java.util.Objects;

public class ContactEntity extends AbstractEntity {
    private String name;
    private String surname;
    private String phoneNumber;

    public ContactEntity() {
    }

    public ContactEntity(String name, String surname, String phoneNumber) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
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
        return Objects.equals(name, that.name) && Objects.equals(surname, that.surname) && Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, phoneNumber);
    }

    @Override
    public String toString() {
        return "ContactEntity{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
