package com.mkomarov.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MediafileDownloadDto {
    private final byte[] fileData;
    private final String contentType;
    private final Long contentLength;
}
