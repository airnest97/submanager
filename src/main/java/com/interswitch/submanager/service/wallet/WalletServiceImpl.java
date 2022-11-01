package com.interswitch.submanager.service.wallet;

import com.interswitch.submanager.dtos.requests.CreateWalletRequest;
import com.interswitch.submanager.dtos.requests.WalletTransactionRequest;
import com.interswitch.submanager.dtos.responses.CreateWalletResponse;
import com.interswitch.submanager.dtos.responses.WalletTransactionResponse;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.models.data.Wallet;
import com.interswitch.submanager.models.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Override
    public CreateWalletResponse createWallet(CreateWalletRequest createWalletRequest) {
        if (createWalletRequest.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            Wallet wallet = Wallet.builder()
                    .balance(createWalletRequest.getBalance())
                    .walletAddress(generateWalletAddress())
                    .build();
            Wallet savedWallet = walletRepository.save(wallet);
            return new CreateWalletResponse(true, savedWallet.getId());
        }
        throw new SubmanagerException("You do not have sufficient balance to create wallet", 400);
    }

    @Override
    public Wallet findWalletById(long id) {
        return walletRepository.findById(id).orElseThrow(() -> new SubmanagerException("Wallet not found!", 404));
    }

    @Override
    public WalletTransactionResponse depositFunds(WalletTransactionRequest walletTransactionRequest) {
        Wallet foundWallet = walletRepository.findById(walletTransactionRequest.getWalletId()).
                orElseThrow(() -> new SubmanagerException("Wallet with Id " + walletTransactionRequest.getWalletId() + " not found!", 404));
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String key = System.getenv("PAY_STACK_KEY");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + key);
        walletTransactionRequest.setAmount(String.valueOf(Integer.parseInt(walletTransactionRequest.getAmount()) * 100));
        log.info(walletTransactionRequest.getAmount());
        log.info(walletTransactionRequest.getEmail());
        HttpEntity<WalletTransactionRequest> requestEntity = new HttpEntity<>(walletTransactionRequest, headers);
        String url = "https://api.paystack.co/transaction/initialize";
        ResponseEntity<WalletTransactionResponse> response =
                restTemplate.postForEntity(url, requestEntity, WalletTransactionResponse.class);
        if (Objects.requireNonNull(response.getBody()).getStatus().equals("true")) {
            foundWallet.setBalance(foundWallet.getBalance().add(BigDecimal.valueOf(
                    Long.parseLong(walletTransactionRequest.getAmount()))));
            walletRepository.save(foundWallet);
            return response.getBody();
        }
        throw new SubmanagerException(response.getBody().getMessage(), 400);
    }

    private String generateWalletAddress(){
        String walletAddress = UUID.randomUUID().toString().substring(0, 10);
        if (walletRepository.existsByWalletAddress(walletAddress)){
            generateWalletAddress();
        }
        return walletAddress;
    }
}

