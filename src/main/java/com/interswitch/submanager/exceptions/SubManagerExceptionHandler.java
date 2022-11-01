package com.interswitch.submanager.exceptions;

import com.interswitch.submanager.dtos.responses.PaymentApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URISyntaxException;

@RestControllerAdvice
public class SubManagerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(SubmanagerException.class)
    public ResponseEntity<PaymentApiResponse> handleSubManagerException(SubmanagerException submanagerException){
        PaymentApiResponse apiResponse = PaymentApiResponse.builder()
                .successful(false)
                .subscriptionPaymentResponse(null)
                .message(submanagerException.getMessage())
                .status("unsuccessful")
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler( HttpClientErrorException.class)
    public ResponseEntity<?> handleHttpClientErrorException( HttpClientErrorException httpClientErrorException){
        return new ResponseEntity<>(httpClientErrorException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(URISyntaxException.class)
    public ResponseEntity<?> handleSubManagerException(URISyntaxException uriSyntaxException){
        return new ResponseEntity<>(uriSyntaxException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}