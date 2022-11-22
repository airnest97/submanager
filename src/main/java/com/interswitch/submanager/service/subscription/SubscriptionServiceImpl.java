package com.interswitch.submanager.service.subscription;

import com.interswitch.submanager.dtos.requests.*;
import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.dtos.responses.PaymentApiResponse;
import com.interswitch.submanager.dtos.responses.SubscriptionPaymentResponse;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.models.data.Subscription;
import com.interswitch.submanager.models.data.User;
import com.interswitch.submanager.models.data.Wallet;
import com.interswitch.submanager.models.enums.Category;
import com.interswitch.submanager.models.enums.Cycle;
import com.interswitch.submanager.models.enums.RecurringPayment;
import com.interswitch.submanager.models.repositories.SubscriptionRepository;
import com.interswitch.submanager.service.subscription.pdfService.SubscriptionPdfExporter;
import com.interswitch.submanager.service.subscription.pdfService.SubscriptionPdfExporterImpl;
import com.interswitch.submanager.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ModelMapper modelMapper;
    private final WalletService walletService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders headers = new HttpHeaders();
    private URI uri;

    @Override
    public Subscription addSubscription(User user, AddSubscriptionRequest request) throws SubmanagerException {
        Optional<Subscription> foundSubscription = user.getSubscriptions().stream().
                filter(subscription -> subscription.getNameOfSubscription().equals(request.getNameOfSubscription())).findFirst();
        if (foundSubscription.isPresent()) {
            throw new SubmanagerException(String.format("%s already exist", request.getNameOfSubscription()), 400);
        }
        Subscription subscription = modelMapper.map(request, Subscription.class);
        subscription.setUser(user);
        subscription.setDateAdded(LocalDate.now());
        subscription.setCategory(request.getCategory());
        return subscriptionRepository.save(subscription);
    }


    @Override
    public ResponseEntity<?> payForDstvSubscriptionWithCard(@NotNull DstvCardPaymentRequest dstvCardPaymentRequest) throws URISyntaxException {
        Subscription foundSubscription = subscriptionRepository.findSubscriptionById(Long.parseLong(dstvCardPaymentRequest.getSubscriptionId())).
                orElseThrow(() -> new SubmanagerException("Subscription with subscription Id " + dstvCardPaymentRequest.getSubscriptionId() + " not found", 404));
        final String baseUrl = "https://submanage-pay.herokuapp.com/api/v1/payment/cableTransaction/dstvCard";
        headers.setContentType(MediaType.APPLICATION_JSON);
        uri = new URI(baseUrl);
        HttpEntity<DstvCardPaymentRequest> requestEntity = new HttpEntity<>(dstvCardPaymentRequest, headers);
        ResponseEntity<PaymentApiResponse> result = restTemplate.postForEntity(uri, requestEntity, PaymentApiResponse.class);
        if (Objects.requireNonNull(result.getBody()).isSuccessful()) {
            updateSubscriptionDetailsAfterPayment(foundSubscription, result);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> payForDstvSubscriptionWithWallet(DstvWalletPaymentRequest dstvWalletPaymentRequest) throws URISyntaxException {
        Subscription foundSubscription = subscriptionRepository.findSubscriptionById(Long.parseLong(dstvWalletPaymentRequest.getSubscriptionId())).
                orElseThrow(() -> new SubmanagerException("Subscription with subscription Id " + dstvWalletPaymentRequest.getSubscriptionId() + " not found", 404));
        Wallet foundWallet = walletService.findWalletById(Long.parseLong(dstvWalletPaymentRequest.getWalletId()));
        if (foundWallet.getBalance().compareTo(dstvWalletPaymentRequest.getPriceOfSubscription()) < 0) {
            throw new SubmanagerException("You do not have sufficient balance in your wallet. Please load your wallet or explore other payment channels ", 400);
        }
        foundWallet.setBalance(foundWallet.getBalance().subtract(dstvWalletPaymentRequest.getPriceOfSubscription()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        final String baseUrl = "https://submanage-pay.herokuapp.com/api/v1/payment/cableTransaction/dstvWallet";
        uri = new URI(baseUrl);
        HttpEntity<DstvWalletPaymentRequest> requestEntity = new HttpEntity<>(dstvWalletPaymentRequest, headers);
        ResponseEntity<PaymentApiResponse> result = restTemplate.postForEntity(uri, requestEntity, PaymentApiResponse.class);
        if (Objects.requireNonNull(result.getBody()).isSuccessful()) {
            updateSubscriptionDetailsAfterPayment(foundSubscription, result);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> payForGotvSubscriptionWithCard(GotvCardPaymentRequest gotvCardPaymentRequest) throws URISyntaxException {
        Subscription foundSubscription = subscriptionRepository.findSubscriptionById(Long.parseLong(gotvCardPaymentRequest.getSubscriptionId())).
                orElseThrow(() -> new SubmanagerException("Subscription with subscription Id " + gotvCardPaymentRequest.getSubscriptionId() + " not found", 404));
        final String baseUrl = "https://submanage-pay.herokuapp.com/api/v1/payment/cableTransaction/gotvCard";
        headers.setContentType(MediaType.APPLICATION_JSON);
        uri = new URI(baseUrl);
        HttpEntity<GotvCardPaymentRequest> requestEntity = new HttpEntity<>(gotvCardPaymentRequest, headers);
        ResponseEntity<PaymentApiResponse> result = restTemplate.postForEntity(uri, requestEntity, PaymentApiResponse.class);
        if (Objects.requireNonNull(result.getBody()).isSuccessful()) {
            updateSubscriptionDetailsAfterPayment(foundSubscription, result);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> payForGotvSubscriptionWithWallet(GotvWalletPaymentRequest gotvWalletPaymentRequest) throws URISyntaxException {
        Subscription foundSubscription = subscriptionRepository.findSubscriptionById(Long.parseLong(gotvWalletPaymentRequest.getSubscriptionId())).
                orElseThrow(() -> new SubmanagerException("Subscription with subscription Id " + gotvWalletPaymentRequest.getSubscriptionId() + " not found", 404));
        Wallet foundWallet = walletService.findWalletById(Long.parseLong(gotvWalletPaymentRequest.getWalletId()));
        if (foundWallet.getBalance().compareTo(gotvWalletPaymentRequest.getPriceOfSubscription()) < 0) {
            throw new SubmanagerException("You do not have sufficient balance in your wallet. Please load your wallet or explore other payment channels ", 400);
        }
        foundWallet.setBalance(foundWallet.getBalance().subtract(gotvWalletPaymentRequest.getPriceOfSubscription()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        final String baseUrl = "https://submanage-pay.herokuapp.com/api/v1/payment/cableTransaction/gotvWallet";
        uri = new URI(baseUrl);
        HttpEntity<GotvWalletPaymentRequest> requestEntity = new HttpEntity<>(gotvWalletPaymentRequest, headers);
        ResponseEntity<PaymentApiResponse> result = restTemplate.postForEntity(uri, requestEntity, PaymentApiResponse.class);
        if (Objects.requireNonNull(result.getBody()).isSuccessful()) {
            updateSubscriptionDetailsAfterPayment(foundSubscription, result);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> payForMobileDataSubscriptionWithWallet(MobileDataWalletPaymentRequest mobileDataWalletPaymentRequest) throws URISyntaxException {
        Subscription foundSubscription = subscriptionRepository.findSubscriptionById(Long.parseLong(mobileDataWalletPaymentRequest.getSubscriptionId())).
                orElseThrow(() -> new SubmanagerException("Subscription with subscription Id " + mobileDataWalletPaymentRequest.getSubscriptionId() + " not found", 404));
        Wallet foundWallet = walletService.findWalletById(Long.parseLong(mobileDataWalletPaymentRequest.getWalletId()));
        if (foundWallet.getBalance().compareTo(mobileDataWalletPaymentRequest.getPriceOfSubscription()) < 0) {
            throw new SubmanagerException("You do not have sufficient balance in your wallet. Please load your wallet or explore other payment channels ", 400);
        }
        foundWallet.setBalance(foundWallet.getBalance().subtract(mobileDataWalletPaymentRequest.getPriceOfSubscription()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        final String baseUrl = "https://submanage-pay.herokuapp.com/api/v1/payment/dataTransaction/wallet";
        uri = new URI(baseUrl);
        HttpEntity<MobileDataWalletPaymentRequest> requestEntity = new HttpEntity<>(mobileDataWalletPaymentRequest, headers);
        ResponseEntity<PaymentApiResponse> result = restTemplate.postForEntity(uri, requestEntity, PaymentApiResponse.class);
        if (Objects.requireNonNull(result.getBody()).isSuccessful()) {
            updateSubscriptionDetailsAfterPayment(foundSubscription, result);
        }
        return result;
    }

    @Override
    public ResponseEntity<?> payForMobileDataSubscriptionWithCard(MobileDataCardPaymentRequest mobileDataCardPaymentRequest) throws URISyntaxException {
        Subscription foundSubscription = subscriptionRepository.findSubscriptionById(Long.parseLong(mobileDataCardPaymentRequest.getSubscriptionId())).
                orElseThrow(() -> new SubmanagerException("Subscription with subscription Id " + mobileDataCardPaymentRequest.getSubscriptionId() + " not found", 404));
        final String baseUrl = "https://submanage-pay.herokuapp.com/api/v1/payment/dataTransaction/card";
        headers.setContentType(MediaType.APPLICATION_JSON);
        uri = new URI(baseUrl);
        HttpEntity<MobileDataCardPaymentRequest> requestEntity = new HttpEntity<>(mobileDataCardPaymentRequest, headers);
        ResponseEntity<PaymentApiResponse> result = restTemplate.postForEntity(uri, requestEntity, PaymentApiResponse.class);
        if (Objects.requireNonNull(result.getBody()).isSuccessful()) {
            updateSubscriptionDetailsAfterPayment(foundSubscription, result);
        }
        return result;
    }

    @Override
    public Subscription updateSubscription(String subscriptionId, UpdateSubscriptionRequest request) throws SubmanagerException {
        Optional<Subscription> found = subscriptionRepository.findSubscriptionById(Long.parseLong(subscriptionId));
        if (found.isEmpty()) {
            throw new SubmanagerException("Subscription Not Found", 404);
        }

        Subscription toBeUpdated = found.get();

        modelMapper.map(request, toBeUpdated);
        return subscriptionRepository.save(toBeUpdated);
    }

    @Override
    public SubscriptionDto findSubscriptionById(String id) throws SubmanagerException {
        Subscription subscription = subscriptionRepository.findSubscriptionById(Long.parseLong(id)).
                orElseThrow(() -> new SubmanagerException("Subscription with Id " + id + " not found", 404));
        return buildResponse(subscription);
    }

    @Override
    public SubscriptionDto findSubscriptionByName(User user, String name) {
       Optional<Subscription> foundSubscription = Optional.ofNullable(user.getSubscriptions().stream().filter(subscription ->
               subscription.getNameOfSubscription().equals(name)).findFirst().orElseThrow(() -> new SubmanagerException("No subscription found!", 404)));
       if(foundSubscription.isEmpty()){
           throw new SubmanagerException("Subscription with name as "+name+" not found", 404);
       }
        return modelMapper.map(foundSubscription, SubscriptionDto.class);
    }

    @Override
    public Map<String, Object> findAll(int numberOfPages, int numberOfItems) {
        Pageable pageable = PageRequest.of(numberOfPages, numberOfItems, Sort.by("nameOfSubscription"));
        Page<Subscription> page = subscriptionRepository.findAll(pageable);
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("totalNumberOfPages", page.getTotalPages());
        pageResult.put("totalNumberOfElementsInDatabase", page.getTotalElements());
        if (page.hasNext()) {
            pageResult.put("nextPage", page.nextPageable());
        }
        if (page.hasPrevious()) {
            pageResult.put("previousPage", page.previousPageable());
        }
        pageResult.put("subscriptions", page.getContent());
        pageResult.put("NumberOfElementsInPage", page.getNumberOfElements());
        pageResult.put("pageNumber", page.getNumber());
        pageResult.put("size", page.getSize());
        return pageResult;
    }

    @Override
    public List<SubscriptionDto> getAllSubscriptionForUser(Long id) {
        List<Subscription> list = subscriptionRepository.findSubscriptionByUser_Id(id).orElseThrow(
                () -> new SubmanagerException("User not found!", 404));
        List<SubscriptionDto> subscriptionDtoList = new ArrayList<>();
        for (Subscription subscription : list) {
            SubscriptionDto subscriptionDto = modelMapper.map(subscription, SubscriptionDto.class);
            subscriptionDtoList.add(subscriptionDto);
        }
        return subscriptionDtoList;
    }

    @Override
    public Subscription removeSubscription(String subscriptionId) throws SubmanagerException {
        var toBeDeleted = subscriptionRepository.findSubscriptionById(Long.parseLong(subscriptionId));
        if (toBeDeleted.isEmpty()) {
            throw new SubmanagerException("Subscription not found", 404);
        }

        Subscription subscription = toBeDeleted.get();
        subscriptionRepository.delete(subscription);
        return subscription;
    }

    @Override
    public void generateSubscriptionReport(String numberOfMonths, String userId, HttpServletResponse response) throws IOException, SubmanagerException {
        List<Subscription> subscriptions = subscriptionRepository.findSubscriptionByUser_Id(Long.parseLong(userId)).orElseThrow(
                () -> new SubmanagerException("No subscription found!", 404));

        if (Integer.parseInt(numberOfMonths) > 0 && Integer.parseInt(numberOfMonths) <= 12) {
            List<SubscriptionDto> filteredSubscriptionResponses = filterSubscriptionByDate(numberOfMonths, subscriptions);

            response.setContentType("application/pdf");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());

            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=users_" + currentDateTime + ".pdf";
            response.setHeader(headerKey, headerValue);

            SubscriptionPdfExporter exporter = new SubscriptionPdfExporterImpl(filteredSubscriptionResponses);
            exporter.export(response);
        } else {
            throw new SubmanagerException("Number of months is not valid. Kindly check again..", 404);
        }
    }

    private List<SubscriptionDto> filterSubscriptionByDate(String numberOfMonths, List<Subscription> subscriptions) {
        return subscriptions.stream().filter(subscription ->
                subscription.getDateAdded().isEqual(LocalDate.now().minusMonths(Integer.parseInt(numberOfMonths))) ||
                        subscription.getDateAdded().isAfter(LocalDate.now().
                                minusMonths(Integer.parseInt(numberOfMonths)))).map(this::buildResponse).toList();
    }

    @Override
    public List<Subscription> findByDate(LocalDate now) {
        return subscriptionRepository.findByDate(now);
    }

    private SubscriptionDto buildResponse(Subscription savedSubscription) {
        return getSubscriptionDto(savedSubscription);
    }

    public static SubscriptionDto getSubscriptionDto(Subscription savedSubscription) {
        return SubscriptionDto.builder()
                .nextPayment(savedSubscription.getNextPayment())
                .dateAdded(savedSubscription.getDateAdded())
                .recurringPayment(RecurringPayment.valueOf(String.valueOf(savedSubscription.getRecurringPayment())))
                .description(savedSubscription.getDescription())
                .id(savedSubscription.getId())
                .priceOfSubscription(savedSubscription.getPriceOfSubscription())
                .paymentCycle(Cycle.valueOf(String.valueOf(savedSubscription.getPaymentCycle())))
                .category(Category.valueOf(String.valueOf(savedSubscription.getCategory())))
                .nameOfSubscription(savedSubscription.getNameOfSubscription())
                .build();
    }

    private void updateSubscriptionDetailsAfterPayment(Subscription foundSubscription, ResponseEntity<PaymentApiResponse> result) {
        SubscriptionPaymentResponse response = Objects.requireNonNull(result.getBody()).getSubscriptionPaymentResponse();
        foundSubscription.setDateAdded(response.getDateOfPayment());
        foundSubscription.setPriceOfSubscription(response.getPriceOfSubscription());
        foundSubscription.setNextPayment(response.getNextPaymentDate());
        subscriptionRepository.save(foundSubscription);
    }
}
