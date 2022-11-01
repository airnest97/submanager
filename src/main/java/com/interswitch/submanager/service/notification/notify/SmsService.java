package com.interswitch.submanager.service.notification.notify;

import com.interswitch.submanager.dtos.requests.SmsRequest;
import com.interswitch.submanager.dtos.responses.SMSResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

public interface SmsService {
    SMSResponse sendSms(SmsRequest request) throws UnirestException;
}
