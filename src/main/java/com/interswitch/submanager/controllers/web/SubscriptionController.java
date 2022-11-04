package com.interswitch.submanager.controllers.web;

import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.dtos.requests.*;
import com.interswitch.submanager.dtos.responses.ApiResponse;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
@RequestMapping("api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/id")
    public ResponseEntity<?>findSubscriptionById(@Valid @NotBlank @NotNull @RequestParam String id) throws SubmanagerException {
        SubscriptionDto subscriptionResponse = subscriptionService.findSubscriptionById(id);
        return new ResponseEntity<>(subscriptionResponse, HttpStatus.OK);
    }

    @GetMapping("/{pageNo}/{noOfItems}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllSubscription(
            @PathVariable(value = "pageNo", required = false) @DefaultValue({"0"}) @NotNull String pageNo,
            @PathVariable(value = "noOfItems", required = false) @DefaultValue({"10"}) @NotNull String numberOfItems){

        Map<String, Object> pageResult = subscriptionService.findAll(Integer.parseInt(pageNo), Integer.parseInt(numberOfItems));
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .successful(true)
                .message("pages returned")
                .data(pageResult)
                .result((int)pageResult.get("NumberOfElementsInPage"))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/subscriptions/export/pdf")
    public void generateSubscriptionReport(
            @RequestParam String numberOfMonths,
            @RequestParam String userId, HttpServletResponse response) throws IOException, SubmanagerException {
        subscriptionService.generateSubscriptionReport(numberOfMonths, userId, response);
    }

    @PostMapping("/subscriptions/payment/dstvCard")
    public ResponseEntity<?> payForDstvSubscriptionWithCard(@RequestBody DstvCardPaymentRequest dstvCardPaymentRequest) throws URISyntaxException {
        return subscriptionService.payForDstvSubscriptionWithCard(dstvCardPaymentRequest);
    }

    @PostMapping("/subscriptions/payment/dstvWallet")
    public ResponseEntity<?> payForDstvSubscriptionWithWallet(@RequestBody DstvWalletPaymentRequest dstvWalletPaymentRequest) throws URISyntaxException {
        return subscriptionService.payForDstvSubscriptionWithWallet(dstvWalletPaymentRequest);
    }

    @PostMapping("/subscriptions/payment/gotvCard")
    public ResponseEntity<?> payForGotvSubscriptionWithCard(@RequestBody GotvCardPaymentRequest gotvCardPaymentRequest) throws URISyntaxException {
        return subscriptionService.payForGotvSubscriptionWithCard(gotvCardPaymentRequest);
    }

    @PostMapping("/subscriptions/payment/gotvWallet")
    public ResponseEntity<?> payForGotvSubscriptionWithWallet(@RequestBody GotvWalletPaymentRequest gotvWalletPaymentRequest) throws URISyntaxException {
        return subscriptionService.payForGotvSubscriptionWithWallet(gotvWalletPaymentRequest);
    }

    @PostMapping("/subscriptions/payment/mobileDataWallet")
    public ResponseEntity<?> payForMobileDataSubscriptionWithWallet(@RequestBody MobileDataWalletPaymentRequest mobileDataWalletPaymentRequest) throws URISyntaxException {
        return subscriptionService.payForMobileDataSubscriptionWithWallet(mobileDataWalletPaymentRequest);
    }

    @PostMapping("/subscriptions/payment/mobileDataCard")
    public ResponseEntity<?> payForMobileDataSubscriptionWithCard(@RequestBody MobileDataCardPaymentRequest mobileDataCardPaymentRequest) throws URISyntaxException {
        return subscriptionService.payForMobileDataSubscriptionWithCard(mobileDataCardPaymentRequest);
    }
}
