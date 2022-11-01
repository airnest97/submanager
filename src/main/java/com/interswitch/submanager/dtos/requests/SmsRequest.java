package com.interswitch.submanager.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRequest {
    private String message;
    private String receiverPhoneNumber;
}
