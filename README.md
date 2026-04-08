# Distopia

Application web de gestion d'un parc de cinémas, développée dans le cadre d'une évaluation  
Spring Boot / JPA / Thymeleaf

## Table des matières
- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)

## Présentation

Distopia est une application web permettant de consulter et gérer un parc de cinémas répartis en France où  
les visiteurs peuvent rechercher des cinémas par ville ou par mot-clé, consulter les films à l'affiche et les séances disponibles.  
Les utilisateurs connectés peuvent réserver une ou plusieurs places pour une séance donnée.  
L'administrateur dispose d'une interface complète pour gérer les villes, cinémas, films et séances.

> L'authentification est gérée via session HTTP avec hachage des mots de passe BCrypt
---

## Fonctionnalités

### Visiteur (non connecté) 
- Rechercher des cinémas par **ville** et/ ou par **mot-clé**
- Afficher tous les **films à l'affiche** d'un cinéma
- Consulter les **séances disponibles** d'un film avec prix et nombre de places restantes
- S'inscrire/ se connecter

### Utilisateur connecté
- **Réserver une ou plusieurs places** pour une séance (avec contrôle du nombre de places dispos)
- Consulter l'**historique de ses réservations**

### Administrateur
- Gérer les **Villes** : ajouter, modifier, supprimer (les cinémas associés conservent leur existence, leur ville passe à `null`)
- Gérer les **Cinémas** : ajouter, modifier, supprimer, associer à une ville
- Gérer les **Films** : ajouter, modifier, suppression logique (*soft delete* -le film reste en bdd)
- Gérer les **Séances** : ajouter, modifier, supprimer (bloqué si des réservations existent)

---