package com.example.sgalb.Services.Serveur;

import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import com.example.sgalb.Utils.EncryptionUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ServeurServiceInterfaceImpl implements ServeurServiceInterface {
    ServeurRepository serveurRepository;
    UtilisateurRepository utilisateurRepository;
    @Override
    public Serveur ajouterServeur(Serveur serveur, Long idUser) {
        Utilisateur utilisateur = utilisateurRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifie s’il existe déjà un serveur avec le même host et user pour ce user
        Optional<Serveur> existingServeur = serveurRepository.findByHostAndUserAndUtilisateur_IdUtilisateur(
                serveur.getHost(),
                serveur.getUser(),
                idUser
        );

        if (existingServeur.isPresent()) {
            throw new RuntimeException("Ce serveur existe déjà pour cet utilisateur.");
        }

        // Crypter le mot de passe
        String encryptedPassword = EncryptionUtils.encrypt(serveur.getPassword());
        serveur.setPassword(encryptedPassword);

        // Associer au user
        serveur.setUtilisateur(utilisateur);

        return serveurRepository.save(serveur);
    }


    @Override
    public Serveur modifierServeur(Long idUtilisateur, Serveur nouveauServeur) {
        // 🔍 Récupérer le serveur connecté de l'utilisateur
        List<Serveur> serveursConnectes = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);

        if (serveursConnectes.isEmpty()) {
            throw new RuntimeException("❌ Aucun serveur connecté trouvé pour cet utilisateur.");
        }

        Serveur serveurActuel = serveursConnectes.get(0);

        // 🛑 Vérifie si un autre serveur possède déjà le même host + user
        Optional<Serveur> serveurExistant = serveurRepository.findByHostAndUser(
                nouveauServeur.getHost(), nouveauServeur.getUser()
        );

        if (serveurExistant.isPresent() && !serveurExistant.get().getIdServeur().equals(serveurActuel.getIdServeur())) {
            throw new RuntimeException("❌ Ce couple (host, user) existe déjà pour un autre serveur. Veuillez vous déconnecter du dashboard pour y accéder.");
        }

        // ✅ Mise à jour autorisée
        serveurActuel.setHost(nouveauServeur.getHost());
        serveurActuel.setUser(nouveauServeur.getUser());

        // 🔐 Cryptage du mot de passe
        String encryptedPassword = EncryptionUtils.encrypt(nouveauServeur.getPassword());
        serveurActuel.setPassword(encryptedPassword);

        return serveurRepository.save(serveurActuel);
    }




    @Override
    public Serveur getServeur(Long idUtilisateur) {
        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        serveur.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
        return serveur;
    }
//public Optional<Serveur> getServeur(Long idUtilisateur) {
//    return serveurRepository.findFirstByUtilisateurIdUtilisateurAndPasswordIsNotNull(idUtilisateur);
//}
public Optional<Serveur> verifierConnexionServeur(String host, String username, String rawPassword, Long idUtilisateur) {
    Optional<Serveur> serveurOpt = serveurRepository.findByHostAndUserAndUtilisateur_IdUtilisateur(host, username, idUtilisateur);

    if (serveurOpt.isPresent()) {
        Serveur serveur = serveurOpt.get();
        String motDePasseCrypte = serveur.getPassword();

        try {
            String motDePasseDecrypte = EncryptionUtils.decrypt(motDePasseCrypte);

            if (rawPassword.equals(motDePasseDecrypte)) {
                return Optional.of(serveur);
            }
        } catch (Exception e) {
            System.err.println("Erreur de déchiffrement : " + e.getMessage());
        }
    }

    return Optional.empty();
}




    // decrypt du password
//    public Serveur getServeurWithDecryptedPassword(Long idUtilisateur) {
//        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//        if (serveur != null && serveur.getPassword() != null) {
//            String decryptedPassword = EncryptionUtils.decrypt(serveur.getPassword());
//            serveur.setPassword(decryptedPassword);
//        }
//        return serveur;
//    }



    @Override
    public Optional<Serveur> getServeurByHostAndUsernameAndUtilisateur(String host, String username, Long idUtilisateur) {
        return serveurRepository.findByHostAndUserAndUtilisateur_IdUtilisateur(host, username, idUtilisateur);
    }

    public List<Serveur> getServeursWithDecryptedPasswords(Long idUtilisateur) {
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);

        for (Serveur serveur : serveurs) {
            if (serveur.getPassword() != null) {
                String decryptedPassword = EncryptionUtils.decrypt(serveur.getPassword());
                serveur.setPassword(decryptedPassword);
            }
        }

        return serveurs;
    }


    public List<Serveur> getServeursByUtilisateur(Long idUtilisateur) {
        return serveurRepository.findByUtilisateur_IdUtilisateur(idUtilisateur);
    }

    public void setServeurConnecte(Long idUtilisateur, Long idServeurConnecte) {
       Serveur serveur = serveurRepository.findByIdServeur(idServeurConnecte);
       serveur.setConnected(true);
       serveurRepository.save(serveur);
    }
    public void deconnecterServeur(Long idUtilisateur, Long idServeur) {
        Serveur serveur = serveurRepository.findByIdServeur(idServeur);
        serveur.setConnected(false);
        serveurRepository.save(serveur);
    }


    @Override
    public String updateServeur(Long idServeur, Serveur serveurUpated) {
        Serveur serveur = serveurRepository.findByIdServeur(idServeur);

        serveur.setHost(serveurUpated.getHost());
        serveur.setUser(serveurUpated.getUser());

        // 🔐 Chiffrement AES
        String encryptedPassword = EncryptionUtils.encrypt(serveurUpated.getPassword());
        serveur.setPassword(encryptedPassword);

        serveur.setOsType(serveurUpated.getOsType());

        serveurRepository.save(serveur);
        return "Serveur mis à jour avec succès.";
    }

    @Override
    public String deleteServeur(Long idServeur) {
        Optional<Serveur> optServeur = serveurRepository.findById(idServeur);
        if (optServeur.isPresent()) {
            serveurRepository.deleteById(idServeur);
            return "Serveur supprimé avec succès.";
        }
        return "Serveur non trouvé.";
    }



}
