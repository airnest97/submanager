package com.interswitch.submanager.models.repositories;

import com.interswitch.submanager.models.data.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findSubscriptionById(long id);

    @Query(nativeQuery = true, value = "SELECT  * from subcription_table" +
            " where ( nextPayment  > now - 3)")
    List<Subscription> findByDate(LocalDate now);

    Optional<List<Subscription>> findSubscriptionByUser_Id(long userId);
}
