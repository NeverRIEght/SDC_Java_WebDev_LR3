package com.mkomarov.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactEntity extends AbstractEntity {
    private long userId;
    private String name;
    private String surname;
    private String phoneNumber;
    private Long mediafileId;
}
