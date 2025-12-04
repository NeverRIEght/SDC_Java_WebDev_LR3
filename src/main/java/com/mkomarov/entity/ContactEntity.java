package com.mkomarov.entity;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactEntity extends AbstractEntity {
    private UserEntity owner;
    private MediafileEntity mediafile;
    private String name;
    private String surname;
    private String phoneNumber;
}
