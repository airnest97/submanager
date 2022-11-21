package com.interswitch.submanager.dtos.requests;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
