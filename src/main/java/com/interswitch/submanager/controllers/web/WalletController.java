package com.interswitch.submanager.controllers.web;

import com.interswitch.submanager.dtos.requests.WalletTransactionRequest;
import com.interswitch.submanager.dtos.responses.WalletTransactionResponse;
import com.interswitch.submanager.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet")
public class WalletController {
    private final WalletService walletService;

    @PostMapping(value = "/fundWallet", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> depositFunds(@RequestBody WalletTransactionRequest walletTransactionRequest){
        WalletTransactionResponse walletTransactionResponse = walletService.depositFunds(walletTransactionRequest);
        return new ResponseEntity<>(walletTransactionResponse, HttpStatus.OK);
    }
}
