package com.interswitch.submanager.userServiceTest;

import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.dtos.UserDto;
import com.interswitch.submanager.dtos.requests.*;
import com.interswitch.submanager.dtos.responses.CreateWalletResponse;
import com.interswitch.submanager.models.data.Subscription;
import com.interswitch.submanager.models.data.User;
import com.interswitch.submanager.models.data.Wallet;
import com.interswitch.submanager.models.enums.Category;
import com.interswitch.submanager.models.enums.Cycle;
import com.interswitch.submanager.models.repositories.UserRepository;
import com.interswitch.submanager.security.jwt.TokenProvider;
import com.interswitch.submanager.service.subscription.SubscriptionService;
import com.interswitch.submanager.service.user.UserService;
import com.interswitch.submanager.service.user.UserServiceImpl;
import com.interswitch.submanager.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.interswitch.submanager.models.enums.RecurringPayment.RECURRING_PAYMENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper mapper;
    private UserService userService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private WalletService walletService;
    @Mock
    private SubscriptionService subscriptionService;
    private User userToReturn;
    private UserDto userDtoToReturn;
    private CreateAccountRequest accountCreationRequest;
    private CreateWalletRequest createWalletRequest;
    private CreateWalletResponse createWalletResponse;
    private Wallet wallet;
    private AddSubscriptionRequest addSubscriptionRequest;
    private Subscription subscriptionToReturn;
    private SubscriptionDto subscriptionDtoToReturn;

    @BeforeEach
    private void setUp(){
        userService = new UserServiceImpl(userRepository,
                applicationEventPublisher,mapper,bCryptPasswordEncoder,walletService,subscriptionService,tokenProvider);

        addSubscriptionRequest = AddSubscriptionRequest.builder()
                .priceOfSubscription(BigDecimal.valueOf(3400.00))
                .nameOfSubscription("Netflix")
                .category(Category.ENTERTAINMENT)
                .description("Subscribed to netflix")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .paymentCycle(Cycle.MONTHLY)
                .build();
        subscriptionToReturn = Subscription.builder()
                .id(1L)
                .priceOfSubscription(BigDecimal.valueOf(3400.00))
                .nameOfSubscription("Netflix")
                .category(Category.ENTERTAINMENT)
                .description("Subscribed to netflix")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .paymentCycle(Cycle.MONTHLY)
                .build();
        subscriptionDtoToReturn = SubscriptionDto.builder()
                .id(1L)
                .priceOfSubscription(BigDecimal.valueOf(3400.00))
                .nameOfSubscription("Netflix")
                .category(Category.ENTERTAINMENT)
                .description("Subscribed to netflix")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .paymentCycle(Cycle.MONTHLY)
                .build();
        accountCreationRequest =
                new CreateAccountRequest("Firstname", "Lastname", "testemail@gmail.com","password", "090876599595");

        List<Subscription> subscriptions = new ArrayList<>();
        userToReturn = User.builder()
                .id(1L)
                .firstName(accountCreationRequest.getFirstName())
                .lastName(accountCreationRequest.getLastName())
                .email(accountCreationRequest.getEmail())
                .password(accountCreationRequest.getPassword())
                .subscriptions(subscriptions)
                .build();

        userDtoToReturn = UserDto.builder()
                .id(1L)
                .firstName(accountCreationRequest.getFirstName())
                .lastName(accountCreationRequest.getLastName())
                .email(accountCreationRequest.getEmail())
                .build();
        createWalletRequest = CreateWalletRequest.builder()
                .balance(BigDecimal.valueOf(1000))
                .build();
        createWalletResponse = CreateWalletResponse.builder()
                .walletId(1L)
                .message(true)
                .build();
        wallet = Wallet.builder()
                .id(1L)
                .user(userToReturn)
                .balance(createWalletRequest.getBalance())
                .build();

    }

    @Test
    public void testThatUserCanCreateAccount(){
        String host = "http://www.localhost:8080/sigup";
        when(userRepository.findUserByEmail("testemail@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userToReturn);
        when(mapper.map(userToReturn, UserDto.class)).thenReturn(userDtoToReturn);
        when(bCryptPasswordEncoder.encode(userToReturn.getPassword())).thenReturn(userToReturn.getPassword());
        when(walletService.createWallet(any(CreateWalletRequest.class))).thenReturn(createWalletResponse);
        when(walletService.findWalletById(any(Long.class))).thenReturn(wallet);
        UserDto userDto = userService.createUserAccount(host,accountCreationRequest);
        verify(userRepository,times(1)).findUserByEmail("testemail@gmail.com");

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getFirstName()).isEqualTo("Firstname");
        assertThat(userDto.getLastName()).isEqualTo("Lastname");
        assertThat(userDto.getEmail()).isEqualTo("testemail@gmail.com");
    }

    @Test
    public void testThatUserCanBeFoundById(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));
        when(mapper.map(userToReturn, UserDto.class)).thenReturn(userDtoToReturn);

        UserDto userDto = userService.findUserById("1");
        verify(userRepository,times(1)).findById(1L);

        assertThat("Firstname").isEqualTo(userDto.getFirstName());
        assertThat(1L).isEqualTo(userDto.getId());
        assertThat("Lastname").isEqualTo(userDto.getLastName());
        assertThat("testemail@gmail.com").isEqualTo(userDto.getEmail());
    }

    @Test
    public void testThatUserCanBeFoundByEmail(){
        when(userRepository.findUserByEmail("testemail@gmail.com")).thenReturn(Optional.of(userToReturn));

        User user = userService.findUserByEmail("testemail@gmail.com");
        verify(userRepository,times(1)).findUserByEmail("testemail@gmail.com");

        assertThat("Firstname").isEqualTo(user.getFirstName());
        assertThat(1L).isEqualTo(user.getId());
        assertThat("Lastname").isEqualTo(user.getLastName());
        assertThat("testemail@gmail.com").isEqualTo(user.getEmail());
    }

    @Test
    public void testThatUserCanUpdateProfile(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));

        UpdateRequest updateRequest = UpdateRequest.builder()
                .email("newtest@gmail.com")
                .firstName("ben")
                .lastName("Kazeem")
                .build();
        User userToReturn = User.builder()
                .id(1L)
                .firstName(updateRequest.getFirstName())
                .lastName(updateRequest.getLastName())
                .email(updateRequest.getEmail())
                .password(accountCreationRequest.getPassword())
                .build();

        UserDto userDtoToReturn = UserDto.builder()
                .id(1L)
                .firstName(updateRequest.getFirstName())
                .lastName(updateRequest.getLastName())
                .email(updateRequest.getEmail())
                .build();
        when(mapper.map(updateRequest, User.class)).thenReturn(userToReturn);
        when(mapper.map(userToReturn, UserDto.class)).thenReturn(userDtoToReturn);
        UserDto userDto = userService.updateUserProfile(String.valueOf(1L),updateRequest);
        verify(userRepository,times(1)).findById(1L);

        assertThat("ben").isEqualTo(userDto.getFirstName());
        assertThat(1L).isEqualTo(userDto.getId());
        assertThat("Kazeem").isEqualTo(userDto.getLastName());
        assertThat("newtest@gmail.com").isEqualTo(userDto.getEmail());
    }

    @Test
    public void testThatUserCanAddSubscription(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));
        when(subscriptionService.addSubscription(userToReturn,addSubscriptionRequest)).thenReturn(subscriptionToReturn);
        when(userRepository.save(any(User.class))).thenReturn(userToReturn);
        when(mapper.map(subscriptionToReturn, SubscriptionDto.class)).thenReturn(subscriptionDtoToReturn);
        SubscriptionDto subscriptionDto = userService.addSubscription(String.valueOf(1L),addSubscriptionRequest);
        assertThat("Subscribed to netflix").isEqualTo(subscriptionDto.getDescription());
        assertThat("Netflix").isEqualTo(subscriptionDto.getNameOfSubscription());
    }

    @Test
    public void testThatUserCanUpdateSubscription(){
        UpdateSubscriptionRequest updateSubscriptionRequest = UpdateSubscriptionRequest.builder()
                .priceOfSubscription(BigDecimal.valueOf(2200.00))
                .nameOfSubscription("Spotify")
                .category(Category.ENTERTAINMENT)
                .description("Subscribed to Spotify")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .build();
        Subscription subscriptionToReturn = Subscription.builder()
                .id(1L)
                .priceOfSubscription(BigDecimal.valueOf(3400.00))
                .nameOfSubscription("Spotify")
                .category(Category.ENTERTAINMENT)
                .description("Subscribed to Spotify")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .paymentCycle(Cycle.MONTHLY)
                .build();
        SubscriptionDto subscriptionDtoToReturn = SubscriptionDto.builder()
                .id(1L)
                .priceOfSubscription(BigDecimal.valueOf(3400.00))
                .nameOfSubscription("Spotify")
                .category(Category.ENTERTAINMENT)
                .description("Subscribed to Spotify")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .paymentCycle(Cycle.MONTHLY)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));
        when(subscriptionService.updateSubscription(String.valueOf(1L),updateSubscriptionRequest)).thenReturn(subscriptionToReturn);
        when(mapper.map(subscriptionToReturn, SubscriptionDto.class)).thenReturn(subscriptionDtoToReturn);
        SubscriptionDto subscriptionDto = userService.updateSubscription(String.valueOf(1L),String.valueOf(1L),updateSubscriptionRequest);
        assertThat("Subscribed to Spotify").isEqualTo(subscriptionDto.getDescription());
        assertThat("Spotify").isEqualTo(subscriptionDto.getNameOfSubscription());
    }

    @Test
    public void userCanDeleteSubscription(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));
        userToReturn.getSubscriptions().add(subscriptionToReturn);
        when((subscriptionService.removeSubscription(String.valueOf(1L)))).thenReturn(subscriptionToReturn);
        when(mapper.map(subscriptionToReturn,SubscriptionDto.class)).thenReturn(subscriptionDtoToReturn);
        SubscriptionDto subscriptionDto = userService.removeSubscription(String.valueOf(1L),String.valueOf(1L));
        assertThat("Subscribed to netflix").isEqualTo(subscriptionDto.getDescription());
        assertThat("Netflix").isEqualTo(subscriptionDto.getNameOfSubscription());
        assertThat(userToReturn.getSubscriptions().size()).isEqualTo(0);
    }

    @Test
    public void userCanFindSubscriptionByNameOfSubscription(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(userToReturn));
        userToReturn.getSubscriptions().add(subscriptionToReturn);
        when(userService.findSubscriptionByName(String.valueOf(1L), "Netflix")).thenReturn(subscriptionDtoToReturn);
        SubscriptionDto subscriptionDto = userService.findSubscriptionByName(String.valueOf(1L), "Netflix");
        assertThat(userToReturn.getSubscriptions().size()).isEqualTo(1);
        assertThat(subscriptionDto.getPriceOfSubscription()).isEqualTo(BigDecimal.valueOf(3400.00));
    }
}
