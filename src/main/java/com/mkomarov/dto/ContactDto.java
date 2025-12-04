package com.mkomarov.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactDto {
    private Long id;
    private String ownerEmail;
    private String name;
    private String surname;
    private String phoneNumber;
}
