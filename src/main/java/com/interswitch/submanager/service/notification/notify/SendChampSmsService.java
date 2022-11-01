package com.interswitch.submanager.service.notification.notify;

import com.interswitch.submanager.dtos.requests.SmsRequest;
import com.interswitch.submanager.dtos.responses.SMSResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.stereotype.Service;

@Service
public class SendChampSmsService implements SmsService{
    @Override
    public SMSResponse sendSms(SmsRequest request) throws UnirestException {
        String pass = System.getenv("SEND_CHAMP_PASS");

        HttpResponse<String> response = Unirest.post("https://api.sendchamp.com/api/v1/sms/send")
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Authorization", "Bearer " + pass)
                .body("{\"to\":[\"" + request.getReceiverPhoneNumber() + "\"],\"message\":\"" + request.getMessage() + "\",\"sender_name\":\"SAlert\",\"route\":\"non_dnd\"}")
                .asString();
        return response.getStatus() == 200 ? new SMSResponse(true) : new SMSResponse(false);
    }
}
