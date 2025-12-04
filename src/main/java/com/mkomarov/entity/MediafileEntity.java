package com.mkomarov.entity;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediafileEntity extends AbstractEntity {
    private String fileName;
    private String hash;
}
