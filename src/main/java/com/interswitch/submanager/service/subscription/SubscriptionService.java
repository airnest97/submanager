package com.interswitch.submanager.service.subscription;

import com.interswitch.submanager.dtos.requests.*;
import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.models.data.Subscription;
import com.interswitch.submanager.models.data.User;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SubscriptionService {
    Subscription addSubscription(User user, AddSubscriptionRequest request) throws SubmanagerException;
    ResponseEntity<?> payForDstvSubscriptionWithCard(DstvCardPaymentRequest dstvCardPaymentRequest) throws URISyntaxException;
    ResponseEntity<?> payForDstvSubscriptionWithWallet(DstvWalletPaymentRequest dstvWalletPaymentRequest) throws URISyntaxException;
    ResponseEntity<?> payForGotvSubscriptionWithCard(GotvCardPaymentRequest gotvCardPaymentRequest) throws URISyntaxException;
    ResponseEntity<?> payForGotvSubscriptionWithWallet(GotvWalletPaymentRequest gotvWalletPaymentRequest) throws URISyntaxException;
    ResponseEntity<?> payForMobileDataSubscriptionWithWallet(MobileDataWalletPaymentRequest mobileDataWalletPaymentRequest) throws URISyntaxException;
    ResponseEntity<?> payForMobileDataSubscriptionWithCard(MobileDataCardPaymentRequest mobileDataCardPaymentRequest) throws URISyntaxException;
    Subscription updateSubscription(String subscriptionId, UpdateSubscriptionRequest request) throws SubmanagerException;
    SubscriptionDto findSubscriptionById(String id) throws SubmanagerException;
    List<SubscriptionDto> findSubscriptionByCategory(String category) throws SubmanagerException;
    SubscriptionDto findSubscriptionByNameOfSubscription(String name) throws SubmanagerException;
    List<SubscriptionDto> findSubscriptionByPaymentCycle(String paymentCycle) throws SubmanagerException;
    List<SubscriptionDto> findSubscriptionByNextPayment(LocalDate nextPayment) throws SubmanagerException;
    List<SubscriptionDto> findSubscriptionByDateAdded(LocalDate dateAdded) throws SubmanagerException;
    Map<String, Object> findAll(int pageNumber, int noOfItems);
    List<SubscriptionDto> getAllSubscriptionForUser(Long id);
    Subscription removeSubscription(String subscriptionId) throws SubmanagerException;
    void generateSubscriptionReport(String numberOfMonths, String userId, HttpServletResponse httpServletResponse) throws IOException, SubmanagerException;
    List<Subscription> findByDate(LocalDate now);
    void deleteAll();
    int getNumberOfSubscriptions();
}
