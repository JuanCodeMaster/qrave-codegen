package com.qrave.qrservice.repository;

import com.qrave.qrservice.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByStatus(String status);
    Optional<UserSubscription> findTopByUserIdOrderByUpdatedAtDesc(Long userId);

}
