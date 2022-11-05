package com.interswitch.submanager.service.user;

import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.dtos.requests.*;
import com.interswitch.submanager.dtos.UserDto;
import com.interswitch.submanager.dtos.responses.ForgotPasswordResponse;
import com.interswitch.submanager.dtos.responses.ResetPasswordResponse;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.models.data.User;

import java.util.List;

public interface UserService {
    UserDto createUserAccount(String host, CreateAccountRequest createAccountRequest) throws SubmanagerException;
    UserDto findUserById(String userId) throws SubmanagerException;
    List<UserDto> findAllUsers();
    UserDto updateUserProfile(String id, UpdateRequest updateRequest) throws SubmanagerException;
    User findUserByEmail(String email) throws SubmanagerException;
    void verifyUser(String token) throws SubmanagerException;
    ForgotPasswordResponse forgotPassword(String host, ForgotPasswordRequest forgotPasswordRequest) throws SubmanagerException;
    ResetPasswordResponse resetPassword(String id, NewPasswordRequest newPasswordRequest) throws SubmanagerException;
    void passwordVerification(String token) throws SubmanagerException;
    SubscriptionDto addSubscription(String userId, AddSubscriptionRequest request) throws SubmanagerException;
    SubscriptionDto updateSubscription(String userId, String subscriptionId, UpdateSubscriptionRequest request) throws SubmanagerException;
    SubscriptionDto removeSubscription(String userId, String subscriptionId) throws SubmanagerException;
    SubscriptionDto findSubscriptionByName(String userId, String name);
}
