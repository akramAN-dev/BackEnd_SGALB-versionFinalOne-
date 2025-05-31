package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Notification findByIdNotif(Long idNotification);
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n WHERE n.message = :message")
    boolean existsByMessage(@Param("message") String message);
    List<Notification> findByUtilisateurIdUtilisateur(Long idUtilisateur);



}
