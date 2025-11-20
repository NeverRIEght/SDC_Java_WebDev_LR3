package com.mkomarov.dto;

import java.util.Objects;

public class ContactDto {
    private Long id;
    private String ownerEmail;
    private String name;
    private String surname;
    private String phoneNumber;

    public ContactDto() {
    }

    public ContactDto(Long id, String ownerEmail, String name, String surname, String phoneNumber) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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
        ContactDto that = (ContactDto) o;
        return Objects.equals(id, that.id) && Objects.equals(ownerEmail, that.ownerEmail) && Objects.equals(name, that.name) && Objects.equals(surname, that.surname) && Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerEmail, name, surname, phoneNumber);
    }

    @Override
    public String toString() {
        return "ContactDto{" +
                "id=" + id +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
