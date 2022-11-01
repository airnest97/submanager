package com.interswitch.submanager.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @Email
    private String sender;
    @Email
    private String receiver;
    private String subject;
    private String body;
}
