package com.example.sgalb.Services.MachineServices;

import java.util.ArrayList;
import java.util.List;

import com.example.sgalb.Dtos.ServiceInfoDTO;
import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Services.SshService.SshServiceInterfaceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MachineServicesServiceInterfaceImpl implements MachineServicesServiceInterface {

    SshServiceInterfaceImpl sshService;
    ServeurRepository serveurRepository;

    public List<ServiceInfoDTO> listDesServices(Long idUtilisateur) {
        // Récupérer la liste des serveurs connectés de l'utilisateur
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);

        List<ServiceInfoDTO> services = new ArrayList<>();

        // Pour chaque serveur connecté, exécuter la commande SSH et récupérer les services
        for (Serveur serveur : serveurs) {
            String user = serveur.getUser();
            String host = serveur.getHost();
            String password = serveur.getPassword();

            String result = sshService.executeCommand(
                    host, user, password,
                    "systemctl list-units --type=service --all --no-legend --no-pager | grep '.service' | awk '{print $1, $4}'"
            );

            if (result != null && !result.isEmpty()) {
                for (String line : result.split("\n")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length == 2 && parts[0].endsWith(".service")) {
                        String fullName = parts[0];
                        String name = fullName.replace(".service", ""); // Retirer le suffixe
                        String rawStatus = parts[1];
                        String status = rawStatus.equalsIgnoreCase("running") ? "running" : "stopped";
                        String type = getServiceType(name); // Utilise le nom sans .service

                        services.add(new ServiceInfoDTO(name, status, type));
                    }
                }
            }
        }

        return services;
    }




    private String getServiceType(String name) {
        if (name.toLowerCase().contains("web") || name.contains("nginx") || name.contains("apache")) return "web";
        if (name.toLowerCase().contains("api")) return "api";
        if (name.toLowerCase().contains("db") || name.contains("mysql") || name.contains("postgres")) return "database";
        return "other";
    }



    @Override
    public String status(String serviceName, Long idUtilisateur) {
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
        StringBuilder combinedResult = new StringBuilder();

        for (Serveur serveur : serveurs) {
            String user = serveur.getUser();
            String host = serveur.getHost();
            String password = serveur.getPassword();

            String result = sshService.executeCommand(host, user, password, "sudo systemctl status " + serviceName);
            combinedResult.append("Serveur: ").append(host).append("\n").append(result).append("\n\n");
        }
        return combinedResult.toString();
    }

    @Override
    public String start(String serviceName, Long idUtilisateur) {
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
        StringBuilder combinedResult = new StringBuilder();

        for (Serveur serveur : serveurs) {
            String user = serveur.getUser();
            String host = serveur.getHost();
            String password = serveur.getPassword();

            String result = sshService.executeCommand(host, user, password, "sudo systemctl start " + serviceName);
            combinedResult.append("Serveur: ").append(host).append(" - ").append(result).append("\n");
        }
        return combinedResult.toString();
    }

    @Override
    public String stop(String serviceName, Long idUtilisateur) {
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
        StringBuilder combinedResult = new StringBuilder();

        for (Serveur serveur : serveurs) {
            String user = serveur.getUser();
            String host = serveur.getHost();
            String password = serveur.getPassword();

            String result = sshService.executeCommand(host, user, password, "sudo systemctl stop " + serviceName);
            combinedResult.append("Serveur: ").append(host).append(" - ").append(result).append("\n");
        }
        return combinedResult.toString();
    }

    @Override
    public String restart(String serviceName, Long idUtilisateur) {
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
        StringBuilder combinedResult = new StringBuilder();

        for (Serveur serveur : serveurs) {
            String user = serveur.getUser();
            String host = serveur.getHost();
            String password = serveur.getPassword();

            String result = sshService.executeCommand(host, user, password, "sudo systemctl restart " + serviceName);
            combinedResult.append("Serveur: ").append(host).append(" - ").append(result).append("\n");
        }
        return combinedResult.toString();
    }

}
