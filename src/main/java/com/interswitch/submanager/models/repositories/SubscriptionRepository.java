package com.interswitch.submanager.models.repositories;

import com.interswitch.submanager.models.data.Subscription;
import com.interswitch.submanager.models.enums.Category;
import com.interswitch.submanager.models.enums.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findSubscriptionByNameOfSubscription(String nameOfSubscription);
    Optional<Subscription> findSubscriptionById(long id);
    Optional<List<Subscription>> findSubscriptionByCategory(Category category);
    Optional<List<Subscription>> findSubscriptionByPaymentCycle(Cycle paymentCycle);
    Optional<List<Subscription>> findSubscriptionByNextPayment(LocalDate nextPayment);
    Optional<List<Subscription>> findSubscriptionByDateAdded(LocalDate dateAdded);
    @Query(nativeQuery = true, value = "SELECT  * from subcription_table" +
            " where ( nextPayment  > now - 3)")
    List<Subscription> findByDate(LocalDate now);
    Optional<List<Subscription>> findSubscriptionByUser_Id(long userId);
}
