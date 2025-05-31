package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Notification;
import com.example.sgalb.Repositories.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class NotifController {
    NotificationRepository notificationRepository;
    @GetMapping("/notifReading")
    public String userReadNotification(@RequestParam Long idNotif)
    {
        Notification notification =notificationRepository.findByIdNotif(idNotif);
        notificationRepository.delete(notification);
        return "Notifcation lue";
    }
//    @GetMapping("/Notifs")
//    public List<Notification> notifications()
//    {
//        return notificationRepository.findAll();
//    }
        @GetMapping("/Notifs")
        public List<Notification> getNotificationsByUtilisateur(@RequestParam Long idUtilisateur) {
            return notificationRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        }

}
