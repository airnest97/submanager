package com.interswitch.submanager.dtos.requests;

import lombok.*;

import javax.validation.constraints.Email;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageRequest extends EmailRequest {
    @Email
    private String sender;
    @Email
    private String receiver;
    private String subject;
    private String body;
    private String usersFullName;
    private String verificationToken;
    private String domainUrl;
}
