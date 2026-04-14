package com.backendev.subscription_tracker.repository;

import com.backendev.subscription_tracker.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByName(String name);

    List<Subscription> findByRenewalDateBetween(LocalDate from, LocalDate to);
}