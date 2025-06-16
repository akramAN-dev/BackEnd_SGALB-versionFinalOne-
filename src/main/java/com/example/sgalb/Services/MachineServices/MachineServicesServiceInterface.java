package com.example.sgalb.Services.MachineServices;

import com.example.sgalb.Dtos.ServiceInfoDTO;

import java.util.List;

public interface MachineServicesServiceInterface {
    //lister tout les services de /etc/systemd/system
    List<ServiceInfoDTO> listDesServices(Long idUtilisateur);
    // status de chaque service
    String status(String serviceName,Long idUtilisateur);
    // demarer un service si il est eteint
    String start(String serviceName,Long idUtilisateur);
    // stoper un service si il est demarer
    String stop(String serviceName,Long idUtilisateur);
    // redemaere un service si il est demarer
    String restart(String serviceName,Long idUtilisateur);

}
