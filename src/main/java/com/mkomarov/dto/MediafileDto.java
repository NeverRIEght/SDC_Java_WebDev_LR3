package com.mkomarov.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediafileDto {
    private Long id;
    private Long associatedContactId;
    private String ownerEmail;
    private String filename;
    private String hash;
    private byte[] fileData;
    private String contentType;
    private long fileSize;
}
