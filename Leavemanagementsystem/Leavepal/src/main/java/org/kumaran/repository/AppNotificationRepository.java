package org.kumaran.repository;

import java.util.List;

import org.kumaran.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    @Modifying
    @Transactional
    void deleteByRecipientUsername(String recipientUsername);
}
