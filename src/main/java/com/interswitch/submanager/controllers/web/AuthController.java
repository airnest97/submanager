package com.interswitch.submanager.controllers.web;

import com.interswitch.submanager.dtos.requests.*;
import com.interswitch.submanager.dtos.responses.ApiResponse;
import com.interswitch.submanager.dtos.UserDto;
import com.interswitch.submanager.dtos.responses.ForgotPasswordResponse;
import com.interswitch.submanager.dtos.responses.ResetPasswordResponse;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.models.data.User;
import com.interswitch.submanager.security.jwt.TokenProvider;
import com.interswitch.submanager.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, TokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createUser(HttpServletRequest request, @RequestBody @Valid @NotNull CreateAccountRequest accountCreationRequest) throws SubmanagerException {
        String host = request.getRequestURL().toString();
        int index = host.indexOf("/", host.indexOf("/", host.indexOf("/"))+2);
        host = host.substring(0, index+1);
        log.info("Host --> {}", host);
        UserDto userDto = userService.createUserAccount(host, accountCreationRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .successful(true)
                .message("user created successfully")
                .data(userDto)
                .build();
        log.info("Returning response");
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }


    @RequestMapping(value = "/verify/{token}", consumes = "application/json", produces = "application/json")
    public ModelAndView verify(@PathVariable("token") String token) throws SubmanagerException {
        userService.verifyUser(token);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("verification_success");
        return modelAndView;
    }


    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws SubmanagerException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                        loginRequest.getPassword())
        );
        log.info("Authentication --> {}", authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = tokenProvider.generateJWTToken(authentication);
        User user = userService.findUserByEmail(loginRequest.getEmail());
        return new ResponseEntity<>(new AuthToken(token, user.getId()), HttpStatus.OK);
    }

    @PostMapping(value = "/password/forgot", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> forgotPassword(HttpServletRequest request, @RequestBody @Valid @NotNull ForgotPasswordRequest forgotPasswordRequest) {
        String host = request.getRequestURL().toString();
        int index = host.indexOf("/", host.indexOf("/", host.indexOf("/"))+2);
        host = host.substring(0, index+1);
        log.info("Host --> {}", host);

        ForgotPasswordResponse forgotPasswordResponse = userService.forgotPassword(host, forgotPasswordRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .message("user found")
                .successful(true)
                .data(forgotPasswordResponse)
                .build();
        log.info("Returning response");
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping(value = "/password/reset/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> resetPassword(@PathVariable String id, @RequestBody NewPasswordRequest newPasswordRequest){
        ResetPasswordResponse resetPasswordResponse = userService.resetPassword(id, newPasswordRequest);

        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .successful(true)
                .message("Password changed successful")
                .data(resetPasswordResponse)
                .build();
        log.info("Returning response");
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/verifyPasswordToken/{token}", consumes = "application/json", produces = "application/json")
    public ModelAndView verifyUserPassword(@PathVariable("token") String token) {
        userService.passwordVerification(token);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("password_verification");
        return modelAndView;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }));
        return errors;
    }
}
