# Distopia

Application web de gestion d'un parc de cinémas, développée dans le cadre d'une évaluation  
Spring Boot / JPA / Thymeleaf

## Table des matières
- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Installation](#installation)
- [Base de données](#base-de-données)
- [Utilisation](#utilisation)

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

## Architecture

L'application suit une **architecture MVC multi-couches** :

- **View Layer** : Thymeleaf + Bootstrap 5 - templates avec héritage de layout
- **Controller**: Spring MVC @Controller - gestion des requêtes HTTP
- **Service** : Logique métier - validation, transactions, règles de gestion
- **Repository** : Accès base de données via JpaRepository (Spring Data JPA)
- **Entity** :Objets mappés JPA
- **Security** :Authentification par session + BCrypt via Spring Security
- **Database** : MariaDB

---

## Technologies

| Composant           | Technologie                                |
|---------------------|--------------------------------------------|
| **Langage**         | Java 17                                    |
| **Framework**       | Spring Boot 4.x                            |
| **Vue**             | Thymeleaf + Thymeleaf Layout               |
| **ORM**             | Spring Data JPA / Hibernate                |
| **Sécurité**        | Spring Security (BCrypt, session custom)   |
| **Base de données** | MariaDB                                    |
| **Frontend**        | Bootstrap 5.3, Bootstrap Icons, vanilla JS |
| **Build**           | Maven                                      |
| **IDE**             | IntelliJ IDEA                              |

---

## Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/john7440/Distopia.git
```

### 2. Ouvrir dans IntelliJ IDEA

1. `File` -> `Open` -> Sélectionner le dossier du projet
2. Attendre qu'IntelliJ indexe le projet et télécharge les dépendances Maven
3. Vérifier que `pom.xml` est bien reconnu

---

## Base de données

La configuration se trouve dans `src/main/resources/application.properties` :

```properties
# bdd
spring.datasource.url=jdbc:mariadb://localhost:3308/distopia?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
```

> **Note** : Adaptez le port, l'utilisateur et le mot de passe selon votre configuration MariaDB locale

--- 
## Utilisation

### Lancer l'application

**Option A - IntelliJ IDEA :**

Naviguer vers `src/main/java/fr/fms/Distopia/DistopiaApplication.java`  
Clic droit -> `Run 'DistopiaApplication.main()'`

**Option B - Maven :**

```bash
mvn spring-boot:run
```
Puis ouvrir votre navigateur à l'adresse : [http://localhost:8080/index](http://localhost:8080/index)

---
### Tableau des routes

| Route                | Accès    | Description                               |
|----------------------|----------|-------------------------------------------|
| `/index`             | Tous     | Page d'accueil avec recherche             |
| `/cinemas`           | Tous     | Liste des cinémas (filtre ville + mot-clé) |
| `/movies?cinemaId=x` | Tous     | Films d'un cinéma                         |
| `/seances?movieId=x` | Tous     | Séances d'un film                         |
| `/login`             | Tous     | Formulaire de connexion                   |
| `/register`          | tous     | Formulaire d'inscription                  |
| `/logout`            | Connecté | Déconnexion                               |
| `/my-reservations`   | Connecté | Historique des réservations               |
| `/reserve` (POST)    | Connecté | réserver une ou plusieurs places          |
| `/admin/towns`       | Admin    | Gestion des villes                        |
| `/admin/cinemas`     | Admin    | Gestion des cinéma                        |
| `/admin/movies`      | Admin    | Gestion des films                         |
| `/admin/seances`     | Admin    | Gestion des séances                       |

---