package com.subscription.repository;

import com.subscription.model.Subscription;
import com.subscription.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDateTime date);
    
    List<Subscription> findByStatusAndEndDateBetween(SubscriptionStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Subscription> findByUserId(UUID userId);
    
}