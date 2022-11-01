package com.interswitch.submanager.models.repositories;

import com.interswitch.submanager.models.data.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByWalletAddress(String walletAddress);
}
