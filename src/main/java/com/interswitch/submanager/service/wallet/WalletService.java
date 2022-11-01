package com.interswitch.submanager.service.wallet;

import com.interswitch.submanager.dtos.requests.CreateWalletRequest;
import com.interswitch.submanager.dtos.requests.WalletTransactionRequest;
import com.interswitch.submanager.dtos.responses.CreateWalletResponse;
import com.interswitch.submanager.dtos.responses.WalletTransactionResponse;
import com.interswitch.submanager.models.data.Wallet;

public interface WalletService {
    CreateWalletResponse createWallet(CreateWalletRequest createWalletRequest);
    Wallet findWalletById(long id);
    WalletTransactionResponse depositFunds(WalletTransactionRequest walletTransactionRequest);
}
