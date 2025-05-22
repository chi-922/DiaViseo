package com.s206.alert.repository;

import com.s206.alert.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByUserId(Integer userId, Pageable pageable);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
	int markAllAsReadByUserId(@Param("userId") Integer userId);

	void deleteByUserIdAndNotificationId(Integer userId, Long notificationId);

	int deleteAllByUserId(Integer userId);
}
