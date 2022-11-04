package com.interswitch.submanager.service.user;

import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.dtos.requests.*;
import com.interswitch.submanager.dtos.UserDto;
import com.interswitch.submanager.dtos.responses.CreateWalletResponse;
import com.interswitch.submanager.dtos.responses.ForgotPasswordResponse;
import com.interswitch.submanager.dtos.responses.ResetPasswordResponse;
import com.interswitch.submanager.events.SendMessageEvent;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.models.data.Role;
import com.interswitch.submanager.models.data.Subscription;
import com.interswitch.submanager.models.data.User;
import com.interswitch.submanager.models.data.Wallet;
import com.interswitch.submanager.models.repositories.UserRepository;
import com.interswitch.submanager.security.jwt.TokenProvider;
import com.interswitch.submanager.service.subscription.SubscriptionService;
import com.interswitch.submanager.service.wallet.WalletService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final WalletService walletService;
    private final SubscriptionService subscriptionService;
    private final TokenProvider tokenProvider;



    @Override
    public UserDto createUserAccount(String host, CreateAccountRequest createAccountRequest) throws SubmanagerException {
        validateIfUserExist(createAccountRequest);
        User user = new User(createAccountRequest.getFirstName(), createAccountRequest.getLastName(), createAccountRequest.getEmail(), bCryptPasswordEncoder.encode(createAccountRequest.getPassword()));
        user.setPhoneNumber(createAccountRequest.getPhoneNumber());
        user.setCreatedDate(LocalDateTime.now());

        createWalletFor(user);

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateTokenForVerification(savedUser.getEmail());
        String verifyPasswordTokenLink = host + "api/v1/auth/verify/"+token;
        MessageRequest message = MessageRequest.builder()
                .subject("VERIFY EMAIL")
                .sender("ernestehigiator@yahoo.com")
                .body("Hi! Here is the link to verify your submanager account-> " + verifyPasswordTokenLink)
                .receiver(user.getEmail())
                .usersFullName(String.format("%s %s", savedUser.getFirstName(), savedUser.getLastName()))
                .build();
        SendMessageEvent event = new SendMessageEvent(message, "email");
        applicationEventPublisher.publishEvent(event);
        log.info(token);

        return modelMapper.map(savedUser, UserDto.class);
    }

    private void createWalletFor(User user) {
        CreateWalletResponse createWalletResponse = walletService.createWallet(new CreateWalletRequest(BigDecimal.ZERO));
        Wallet foundWallet = walletService.findWalletById(createWalletResponse.getWalletId());
        foundWallet.setUser(user);
    }

    private void validateIfUserExist(CreateAccountRequest createAccountRequest) throws SubmanagerException {
        User User = userRepository.findUserByEmail(createAccountRequest.getEmail()).orElse(null);
        if (User != null) {
            throw new SubmanagerException("User with the email "+createAccountRequest.getEmail()+" already exist",400);
        }
    }

    @Override
    public UserDto findUserById(String userId) throws SubmanagerException {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(
                        ()-> new SubmanagerException(String.format("User with id %s not found" ,userId), 404));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map((user -> modelMapper.map(user, UserDto.class)))
                .toList();
    }

    @Override
    public UserDto updateUserProfile(String id, UpdateRequest updateRequest) throws SubmanagerException {
        User user = userRepository.findById(Long.valueOf(id)).orElseThrow(
                () -> new SubmanagerException("User id not found", 404)
        );
        User savedUsed = modelMapper.map(updateRequest, User.class);
        savedUsed.setId(user.getId());
        savedUsed.setCreatedDate(user.getCreatedDate());
        userRepository.save(savedUsed);
        return modelMapper.map(savedUsed, UserDto.class);
    }

    @Override
    public User findUserByEmail(String email) throws SubmanagerException {
        return userRepository.findUserByEmail(email).orElseThrow(()-> new SubmanagerException("user not found", 400));
    }

    @Override
    public void verifyUser(String token) throws SubmanagerException {
        User user = verifyClaimFrom(token);
        if (user == null){
            throw new SubmanagerException("User id does not exist",404);
        }
        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    public ForgotPasswordResponse forgotPassword(String host, ForgotPasswordRequest forgotPasswordRequest) throws SubmanagerException {
        Optional<User> foundUser = userRepository.findUserByEmail(forgotPasswordRequest.getEmail());
        if (foundUser.isEmpty()) {
            throw new SubmanagerException("User with email " + forgotPasswordRequest.getEmail() + " does not exist", 404);
        } else {
            User user = foundUser.get();

            String token = tokenProvider.generateTokenForVerification(user.getEmail());
            String verifyPasswordTokenLink = host + "api/v1/auth/verifyPasswordToken/"+token;
            MessageRequest message = MessageRequest.builder()
                    .subject("RESET PASSWORD")
                    .sender("ernestehigiator@yahoo.com")
                    .receiver(user.getEmail())
                    .body("Hello, kindly click on this link to confirm that you want to change your password -> "+verifyPasswordTokenLink)
                    .usersFullName(String.format("%s %s", user.getFirstName(), user.getLastName()))
                    .build();
            SendMessageEvent resetPasswordEvent = new SendMessageEvent(message,"email");
            applicationEventPublisher.publishEvent(resetPasswordEvent);
            log.info("token-> {}",token);
            return new ForgotPasswordResponse(user.getEmail(), "Email successfully sent");
        }
    }

    @Override
    public ResetPasswordResponse resetPassword(String id, NewPasswordRequest newPasswordRequest) throws SubmanagerException {
        User foundUser = userRepository.findById(Long.valueOf(id)).orElseThrow(()->new SubmanagerException("user with Id " + id + " does not exist", 404));
        if(foundUser.isPermittedToChangePassword()){
            return getResetPasswordResponse(foundUser, newPasswordRequest);
        }
        throw new SubmanagerException("Ops! You are not permitted to change your password", 400);
    }

    @Override
    public void passwordVerification(String token) throws SubmanagerException {
        User user = verifyClaimFrom(token);
        if (user == null){
            throw new SubmanagerException("User id does not exist",404);
        }
        user.setPermittedToChangePassword(true);
        userRepository.save(user);
    }

    @Override
    public SubscriptionDto addSubscription(String userId, AddSubscriptionRequest request) throws SubmanagerException {
        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new SubmanagerException("user not found", 404));
        Subscription subscription = subscriptionService.addSubscription(user, request);
        user.getSubscriptions().add(subscription);
        MessageRequest message = MessageRequest.builder()
                .subject("SUBSCRIPTION SUCCESSFULLY ADDED")
                .sender("ernestehigiator@yahoo.com")
                .receiver(user.getEmail())
                .body(String.format("Hello %s %s, your %s subscription has been successfully added. Thank you for your continuous patronage.", user.getFirstName(), user.getLastName(), subscription.getNameOfSubscription()))
                .usersFullName(String.format("%s %s", user.getFirstName(), user.getLastName()))
                .build();
        SendMessageEvent addSubscriptionEvent = new SendMessageEvent(message,"email");
        applicationEventPublisher.publishEvent(addSubscriptionEvent);
        userRepository.save(user);
        log.info("name of subscription -> {}",subscription.getNameOfSubscription());
        return modelMapper.map(subscription, SubscriptionDto.class);
    }

    @Override
    public SubscriptionDto updateSubscription(String userId, String subscriptionId, UpdateSubscriptionRequest request) throws SubmanagerException {
        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new SubmanagerException("user not found", 404));
        Subscription subscription = subscriptionService.updateSubscription(subscriptionId, request);
        user.getSubscriptions().add(subscription);
        return modelMapper.map(subscription, SubscriptionDto.class);
    }

    @Override
    public SubscriptionDto removeSubscription(String userId, String subscriptionId) throws SubmanagerException {
        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new SubmanagerException("user not found", 404));
        Subscription subscription = subscriptionService.removeSubscription(subscriptionId);
        user.getSubscriptions().remove(subscription);
        return modelMapper.map(subscription, SubscriptionDto.class);
    }


    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new SubmanagerException("user not found", 404));
        org.springframework.security.core.userdetails.User returnedUser = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthorities(user.getRoles()));
        log.info("Returned user --> {}", returnedUser);
        return returnedUser;
    }

    private User verifyClaimFrom(String token) {
        Claims claims = tokenProvider.getAllClaimsFromJWTToken(token);
        Function<Claims, String> getSubjectFromClaim = Claims::getSubject;
        Function<Claims, Date> getExpirationDateFromClaim = Claims::getExpiration;
        Function<Claims, Date> getIssuedAtDateFromClaim = Claims::getIssuedAt;

        String userId = getSubjectFromClaim.apply(claims);
        if (userId == null){
            throw new SubmanagerException("User id not present in verification token", 404);
        }
        Date expiryDate = getExpirationDateFromClaim.apply(claims);
        if (expiryDate == null){
            throw new SubmanagerException("Expiry Date not present in verification token", 404);
        }
        Date issuedAtDate = getIssuedAtDateFromClaim.apply(claims);

        if (issuedAtDate == null){
            throw new SubmanagerException("Issued At date not present in verification token", 404);
        }

        if (expiryDate.compareTo(issuedAtDate) > 14.4 ){
            throw new SubmanagerException("Verification Token has already expired", 404);
        }

        return findUserByEmail(userId);
    }

    private ResetPasswordResponse getResetPasswordResponse(User user, NewPasswordRequest newPasswordRequest) {
        user.setPassword(bCryptPasswordEncoder.encode(newPasswordRequest.getNewPassword()));
        user.setPermittedToChangePassword(false);
        User savedUser = userRepository.save(user);
        return new ResetPasswordResponse(savedUser.getEmail(), "Password reset successful");
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream().map(
                role -> new SimpleGrantedAuthority(role.getRoleType().name())
        ).collect(Collectors.toSet());
    }
}
