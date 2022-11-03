package com.interswitch.submanager.subscriptionServiceTest;

import com.interswitch.submanager.dtos.SubscriptionDto;
import com.interswitch.submanager.dtos.requests.AddSubscriptionRequest;
import com.interswitch.submanager.dtos.requests.UpdateSubscriptionRequest;
import com.interswitch.submanager.exceptions.SubmanagerException;
import com.interswitch.submanager.models.data.User;
import com.interswitch.submanager.models.enums.Category;
import com.interswitch.submanager.models.enums.Cycle;
import com.interswitch.submanager.models.repositories.UserRepository;
import com.interswitch.submanager.security.jwt.TokenProvider;
import com.interswitch.submanager.service.subscription.SubscriptionService;
import com.interswitch.submanager.service.user.UserService;
import com.interswitch.submanager.service.user.UserServiceImpl;
import com.interswitch.submanager.service.wallet.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.interswitch.submanager.models.enums.RecurringPayment.RECURRING_PAYMENT;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SubscriptionServiceImplTest {
    @Autowired
    private SubscriptionService subscriptionService;
    private AddSubscriptionRequest addSubscriptionRequest1;
    private AddSubscriptionRequest addSubscriptionRequest2;
    private UpdateSubscriptionRequest updateSubscriptionRequest;
    private User user;

    @BeforeEach
    void setUp() throws SubmanagerException {
        user = new User();
        addSubscriptionRequest1 = AddSubscriptionRequest.builder()
                .priceOfSubscription(BigDecimal.valueOf(3400.00))
                .nameOfSubscription("Netflix")
                .category(Category.ENTERTAINMENT)
                .description("Subscribed to netflix")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .paymentCycle(Cycle.MONTHLY)
                .build();
        subscriptionService.addSubscription(user,addSubscriptionRequest1);

        addSubscriptionRequest2 = AddSubscriptionRequest.builder()
                .priceOfSubscription(BigDecimal.valueOf(1500.00))
                .nameOfSubscription("Spotify")
                .category(Category.ENTERTAINMENT)
                .paymentCycle(Cycle.MONTHLY)
                .description("Subscribed to spotify")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .build();
        subscriptionService.addSubscription(user,addSubscriptionRequest2);
    }

    @AfterEach
    public void tearDown(){
        subscriptionService.deleteAll();
    }

    @Test
    @DisplayName("Add subscription test")
    public void subscriptionCanBeAddedTest(){
        assertEquals(2, subscriptionService.getNumberOfSubscriptions());
    }

    @Test
    @DisplayName("No two subscription can have same name test")
    public void subscriptionNameIsUniqueTest(){
        AddSubscriptionRequest addSubscriptionRequest = AddSubscriptionRequest.builder()
                .priceOfSubscription(BigDecimal.valueOf(9500.00))
                .nameOfSubscription("Netflix")
                .category(Category.MUSIC)
                .paymentCycle(Cycle.MONTHLY)
                .description("Subscribed to netflix Hmo")
                .nextPayment(LocalDate.now().plusDays(30))
                .recurringPayment(RECURRING_PAYMENT)
                .build();

        assertThrows(SubmanagerException.class, () -> subscriptionService.addSubscription(user,addSubscriptionRequest));
    }

//    @Test
//    @DisplayName("Find by name of subscription test")
//    public void subscriptionCanBeFoundByNameTest() throws SubmanagerException {
//        SubscriptionDto subscriptionResponse = subscriptionService.findSubscriptionByNameOfSubscription("Netflix");
//        assertNotNull(subscriptionResponse);
//        assertEquals("Subscribed to netflix",subscriptionResponse.getDescription());
//    }
//
//    @Test
//    @DisplayName("Find by category of subscription test")
//    public void subscriptionsCanBeFoundByCategoryTest() throws SubmanagerException {
//        List<SubscriptionDto> subscriptionResponses  = subscriptionService.findSubscriptionByCategory("ENTERTAINMENT");
//        assertNotNull(subscriptionResponses);
//        assertEquals(2,subscriptionResponses.size());
//    }
//
//
//    @Test
//    @DisplayName("Find by Id of subscription test")
//    public void subscriptionCanBeFoundByIdTest() throws SubmanagerException {
//        SubscriptionDto subscriptionResponse = subscriptionService.findSubscriptionByNameOfSubscription("Netflix");
//        SubscriptionDto foundSubscriptionResponse = subscriptionService.findSubscriptionById(String.valueOf(subscriptionResponse.getId()));
//        assertEquals("Subscribed to netflix",foundSubscriptionResponse.getDescription());
//    }
//
//    @Test
//    @DisplayName("Find by next payment of subscription test")
//    public void subscriptionsCanBeFoundByNextPaymentTest() throws SubmanagerException {
//        List<SubscriptionDto> foundSubscriptionResponses  = subscriptionService.findSubscriptionByNextPayment(LocalDate.of(2022,11,22));
//        assertEquals(2,foundSubscriptionResponses.size());
//    }
//
//    @Test
//    @DisplayName("Find by date added of subscription test")
//    public void subscriptionsCanBeFoundByDateAddedTest() throws SubmanagerException {
//        List<SubscriptionDto> subscriptionResponses  = subscriptionService.findSubscriptionByDateAdded(LocalDate.of(2022,10,23));
//        assertEquals("Subscribed to spotify",subscriptionResponses.get(1).getDescription());
//    }
//
//    @Test
//    @DisplayName("Update subscription test")
//    public void subscriptionCanBeUpdatedTest() throws SubmanagerException {
//        updateSubscriptionRequest = UpdateSubscriptionRequest.builder()
//                .category(Category.OTHERS)
//                .nameOfSubscription("Netflix")
//                .description("Subscribing to netflix again")
//                .recurringPayment(RECURRING_PAYMENT)
//                .build();
//
//        SubscriptionDto subscriptionDto = subscriptionService.findSubscriptionByNameOfSubscription("Netflix");
//
//        var response = subscriptionService.updateSubscription(String.valueOf(subscriptionDto.getId()), updateSubscriptionRequest);
//
//        SubscriptionDto foundSubscriptionDto = subscriptionService.findSubscriptionByNameOfSubscription("Netflix");
//        assertEquals("Subscribing to netflix again", foundSubscriptionDto.getDescription());
//
//        assertEquals(subscriptionDto.getId().intValue(), response.getId().intValue());
//    }
//
//    @Test
//    public void subscriptionCanBeRemovedTest() throws SubmanagerException {
//        assertEquals(2, subscriptionService.getNumberOfSubscriptions());
//        SubscriptionDto foundSubscriptionDto = subscriptionService.findSubscriptionByNameOfSubscription("Netflix");
//        subscriptionService.removeSubscription(String.valueOf(foundSubscriptionDto.getId()));
//        assertEquals(1, subscriptionService.getNumberOfSubscriptions());
//    }
}
