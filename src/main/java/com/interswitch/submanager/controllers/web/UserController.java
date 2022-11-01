package com.interswitch.submanager.controllers.web;

import com.interswitch.submanager.dtos.requests.AddSubscriptionRequest;
import com.interswitch.submanager.dtos.requests.UpdateSubscriptionRequest;
import com.interswitch.submanager.dtos.responses.ApiResponse;
import com.interswitch.submanager.dtos.requests.UpdateRequest;
import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.dtos.UserDto;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.service.subscription.SubscriptionService;
import com.interswitch.submanager.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Slf4j
@RequestMapping("api/v1/user")
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class UserController {
    private final UserService userService;
    private final SubscriptionService subscriptionService;


    @Autowired
    public UserController(UserService userService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") @NotNull @NotBlank String userId){
        try {
            if (("null").equals(userId) || ("").equals(userId.trim())){
                throw new SubmanagerException("String id cannot be null", 400);
            }
            UserDto userDto = userService.findUserById(userId);
            Link selfLink = linkTo(UserController.class).slash(userDto.getId()).withSelfRel();
            userDto.add(selfLink);
            ApiResponse apiResponse = ApiResponse.builder()
                    .status("success")
                    .successful(true)
                    .message("user found")
                    .data(userDto)
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (SubmanagerException e) {
            ApiResponse apiResponse = ApiResponse.builder()
                    .status("fail")
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(e.getStatusCode()));
        }
    }
    @GetMapping(value = "/getAllUsers", produces = { "application/hal+json" })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers(){
        List<UserDto> users = userService.findAllUsers();
        for (final UserDto user: users){
            Long userId = user.getId();
            Link selfLink = linkTo(UserController.class).slash(userId).withSelfRel();
            user.add(selfLink);

            List<SubscriptionDto> subscriptions = subscriptionService.getAllSubscriptionForUser(user.getId());

            if (subscriptions.size() > 0){
                Link subscriptionsLink = linkTo(methodOn(UserController.class).getAllSubscriptionsForUser(user.getId())).withRel("subscriptions created");
                user.add(subscriptionsLink);
            }
        }
        Link link = linkTo(UserController.class).withSelfRel();
        CollectionModel<UserDto> result = CollectionModel.of(users, link);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .successful(true)
                .message(users.size() != 0 ? "users found" : "no user exists in database")
                .data(result)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/subscription/{id}")
    public List<SubscriptionDto> getAllSubscriptionsForUser(@PathVariable("id") @Valid @NotBlank @NotNull Long id) {
        return subscriptionService.getAllSubscriptionForUser(id);
    }

    @PatchMapping("/updateUser")
    public ResponseEntity<?> updateUserProfile(@Valid @NotBlank @NotNull @RequestParam String id,
                                               @RequestBody @NotNull UpdateRequest updateRequest ) throws SubmanagerException {

        UserDto userDto = userService.updateUserProfile(id, updateRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .successful(true)
                .message("user found")
                .data(userDto)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
    @PostMapping("/addSubscription/{userId}")
    public ResponseEntity<?> createSubscription(@PathVariable String userId, @RequestBody AddSubscriptionRequest addSubscriptionRequest) throws SubmanagerException {
        return new ResponseEntity<>(userService.addSubscription(userId, addSubscriptionRequest), HttpStatus.CREATED);
    }

    @PatchMapping("/updateSubscription/{userId}/{subscriptionId}")
    public ResponseEntity<?> updateSubscription(@PathVariable String userId,@PathVariable String subscriptionId,@RequestBody UpdateSubscriptionRequest updateSubscriptionRequest) throws SubmanagerException {
        return new ResponseEntity<>(userService.updateSubscription(userId, subscriptionId, updateSubscriptionRequest), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/{subscriptionId}")
    public ResponseEntity<?> removeSubscription(@PathVariable String userId,@PathVariable String subscriptionId) throws SubmanagerException {
        return new ResponseEntity<>(userService.removeSubscription(userId, subscriptionId), HttpStatus.NO_CONTENT);
    }
}
