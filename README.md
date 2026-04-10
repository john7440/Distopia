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
- [Structure du projet](#structure-du-projet)

## Présentation

Distopia est une application web permettant de consulter et gérer un parc de cinémas répartis en France où  
les visiteurs peuvent rechercher des cinémas par ville ou par mot-clé, consulter les films à l'affiche et les séances disponibles.  
Les utilisateurs connectés peuvent réserver une ou plusieurs places pour une séance donnée et voir leurs réservations.  
L'administrateur dispose d'une interface complète pour gérer les villes, cinémas, films et séances.

> L'authentification est gérée via session HTTP avec hachage des mots de passe BCrypt
---

## Fonctionnalités

### Visiteur (non connecté) 
- Rechercher des cinémas par **ville** et/ ou par **mot-clé**
- Afficher tous les **films à l'affiche** d'un cinéma
- Consulter les **séances disponibles** d'un film avec prix et nombre de places restantes
- S'inscrire/ Se connecter

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
- **Service** : Logique métier — validation, transactions, règles de gestion
- **Repository** : Accès base de données via JpaRepository (Spring Data JPA)
- **Entity** :Objets mappés JPA
- **Security** :Authentification par session + BCrypt via Spring Security
- **Database** : MariaDB

---

## Technologies

| Composant           | Technologie                                |
|---------------------|--------------------------------------------|
| **Langage**         | Java 17                                    |
| **Framework**       | Spring Boot 4.0.5                          |
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
2. Attendre qu'IntelliJ indexe le projet et télécharger les dépendances Maven
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

> **Note** : **Adaptez** le port, l'utilisateur et le mot de passe **selon votre configuration** MariaDB locale

> **Important** : vous pouvez créer un admin via la route **/createAdmin** (ID : admin/ Pass : admin123), **n'oubliez pas**
> de supprimer la méthode dans `UserController` après utilisation !

### Données de test

```text
-- Nettoyage
DELETE FROM reservation;
DELETE FROM seance;
DELETE FROM cinema_movie;
DELETE FROM movie;
DELETE FROM cinema;
DELETE FROM town;
DELETE FROM users;

-- Villes
INSERT INTO town (id, name) VALUES
(1, 'Lyon'),
(2, 'Paris'),
(3, 'Marseille'),
(4, 'Bordeaux'),
(5, 'Toulouse');

-- Cinémas
INSERT INTO cinema (id, name, address, town_id) VALUES
(1,  'CGR Lyon Confluence',       '112 Cours Charlemagne, 69002 Lyon',          1),
(2,  'UGC Ciné Cité Lyon',        '35 Cours Bayard, 69007 Lyon',                1),
(3,  'Pathé Vaise',               '28 Rue Paul Cazeneuve, 69009 Lyon',          1),
(4,  'UGC Ciné Cité Les Halles',  '7 Place de la Rotonde, 75001 Paris',         2),
(5,  'Pathé Wepler',              '146 Bvd de Clichy, 75018 Paris',             2),
(6,  'MK2 Bibliothèque',          '128 Av de France, 75013 Paris',              2),
(7,  'Les Variétés',              '37 Rue Vincent Scotto, 13001 Marseille',     3),
(8,  'Pathé Plan de Campagne',    'Centre Commercial, 13170 Marseille',         3),
(9,  'CGR Bordeaux',              '13 Rue Georges Bonnac, 33000 Bordeaux',      4),
(10, 'Pathé Toulouse Wilson',     '16 Rue des Lois, 31000 Toulouse',            5);

-- Films
INSERT INTO movie (id, title, genre, duration, description, image_url, deleted) VALUES
(1,  'Dune : Deuxième Partie',
     'Science-Fiction', 166,
     'Paul Atreides s''unit aux Fremen pour mener la guerre contre les Harkonnen.',
     'https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg',
     false),

(2,  'Inception',
     'Thriller', 148,
     'Un voleur spécialisé dans l''extraction de secrets enfouis au cœur des rêves.',
     'https://image.tmdb.org/t/p/w500/edv5CZvWj09upOsy2Y6IwDhK8bt.jpg',
     false),

(3,  'Interstellar',
     'Science-Fiction', 169,
     'Des astronautes voyagent à travers un trou de ver à la recherche d''une nouvelle planète.',
     'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
     false),

(4,  'Le Comte de Monte-Cristo',
     'Aventure', 178,
     'Edmond Dantès, injustement emprisonné, ourdit une vengeance impitoyable.',
     'https://image.tmdb.org/t/p/w500/spCAxD99U1A6jsiePFl3500qpDt.jpg',
     false),

(5,  'Inside Out 2',
     'Animation', 100,
     'Riley entre au lycée et de nouvelles émotions font irruption dans son monde intérieur.',
     'https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg',
     false),

(6,  'Alien : Romulus',
     'Horreur', 119,
     'Un groupe de jeunes colons se retrouve face à la forme de vie la plus terrifiante.',
     'https://image.tmdb.org/t/p/w500/b33nnKl1GSFbao4l3fZDDqsMx0F.jpg',
     false),

(7,  'Oppenheimer',
     'Drame', 180,
     'L''histoire du physicien américain J. Robert Oppenheimer et de la bombe atomique.',
     'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg',
     false),

(8,  'The Batman',
     'Action', 176,
     'Bruce Wayne traque un tueur en série qui sème la terreur à Gotham City.',
     'https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg',
     false);

-- Associations Cinéma <-> Film 
INSERT INTO cinema_movie (cinema_id, movie_id) VALUES
-- Lyon - CGR Confluence
(1, 1), (1, 2), (1, 3), (1, 7),
-- Lyon - UGC
(2, 1), (2, 4), (2, 5), (2, 8),
-- Lyon - Pathé Vaise
(3, 3), (3, 5), (3, 6),
-- Paris - UGC Les Halles
(4, 1), (4, 2), (4, 4), (4, 7),
-- Paris - Pathé Wepler
(5, 3), (5, 6), (5, 8),
-- Paris - MK2 Bibliothèque
(6, 2), (6, 5), (6, 7),
-- Marseille - Les Variétés
(7, 4), (7, 5), (7, 6),
-- Marseille - Pathé Plan de Campagne
(8, 1), (8, 6), (8, 8),
-- Bordeaux - CGR
(9, 2), (9, 3), (9, 4), (9, 7),
-- Toulouse - Pathé Wilson
(10, 1), (10, 5), (10, 8);

-- Séances
-- Dune 2 — CGR Lyon Confluence (cinema 1)
INSERT INTO seance (id, date_time, available_seats, price, movie_id, cinema_id) VALUES
(1,  '2026-04-10 14:00:00', 80,  9.50,  1, 1),
(2,  '2026-04-10 20:45:00', 40,  11.00, 1, 1),
(3,  '2026-04-11 17:30:00', 0,   11.00, 1, 1),  -- complet

-- Dune 2 — UGC Les Halles Paris (cinema 4)
(4,  '2026-04-10 15:00:00', 90,  9.50,  1, 4),
(5,  '2026-04-11 21:00:00', 55,  11.00, 1, 4),

-- Inception — UGC Lyon (cinema 2)  -- non : pas dans cinema_movie(2,2) donc on prend CGR
-- Inception — CGR Lyon Confluence (cinema 1)
(6,  '2026-04-11 15:00:00', 70,  9.50,  2, 1),
(7,  '2026-04-12 20:00:00', 35,  11.00, 2, 1),

-- Inception — MK2 Bibliothèque Paris (cinema 6)
(8,  '2026-04-11 18:30:00', 80,  9.50,  2, 6),

-- Interstellar — Pathé Vaise Lyon (cinema 3)
(9,  '2026-04-12 16:00:00', 75,  9.50,  3, 3),
(10, '2026-04-12 21:00:00', 30,  11.00, 3, 3),

-- Interstellar — Pathé Wepler Paris (cinema 5)
(11, '2026-04-13 19:30:00', 60,  11.00, 3, 5),

-- Monte-Cristo — UGC Lyon (cinema 2)
(12, '2026-04-13 14:00:00', 90,  8.50,  4, 2),
(13, '2026-04-13 20:30:00', 50,  10.00, 4, 2),

-- Monte-Cristo — Les Variétés Marseille (cinema 7)
(14, '2026-04-14 18:00:00', 45,  8.50,  4, 7),

-- Inside Out 2 — Pathé Vaise Lyon (cinema 3)
(15, '2026-04-10 10:00:00', 120, 8.00,  5, 3),
(16, '2026-04-10 14:30:00', 5,   8.00,  5, 3),  -- presque complet

-- Inside Out 2 — MK2 Bibliothèque Paris (cinema 6)
(17, '2026-04-11 11:00:00', 100, 8.00,  5, 6),

-- Alien Romulus — Pathé Vaise Lyon (cinema 3)
(18, '2026-04-11 22:00:00', 55,  11.00, 6, 3),

-- Alien Romulus — Pathé Plan de Campagne Marseille (cinema 8)
(19, '2026-04-12 21:30:00', 40,  11.00, 6, 8),

-- Oppenheimer — CGR Lyon Confluence (cinema 1)
(20, '2026-04-14 17:00:00', 70,  9.50,  7, 1),
(21, '2026-04-15 20:00:00', 60,  11.00, 7, 1),

-- Oppenheimer — CGR Bordeaux (cinema 9)
(22, '2026-04-14 19:00:00', 80,  9.50,  7, 9),

-- The Batman — UGC Lyon (cinema 2)
(23, '2026-04-15 19:30:00', 65,  9.50,  8, 2),
(24, '2026-04-15 22:30:00', 40,  11.00, 8, 2),

-- The Batman — Pathé Toulouse Wilson (cinema 10)
(25, '2026-04-16 20:00:00', 75,  9.50,  8, 10);

UPDATE movie SET trailer_url = 'https://www.youtube.com/embed/n9xhJrPXop4' WHERE id = 1; -- Dune 2
UPDATE movie SET trailer_url = 'https://www.youtube.com/embed/YoHD9XEInc0' WHERE id = 2; -- Inception
UPDATE movie SET trailer_url = 'https://www.youtube.com/embed/zSWdZVtXT7E' WHERE id = 3; -- Interstellar
UPDATE movie SET trailer_url = 'https://www.youtube.com/embed/LEjhY15eCx0' WHERE id = 5; -- Inside Out 2
UPDATE movie SET trailer_url = 'https://www.youtube.com/embed/sAeNumA6UkQ' WHERE id = 6; -- Alien Romulus
UPDATE movie SET trailer_url = 'https://www.youtube.com/embed/uYPbbksJxIg' WHERE id = 7; -- Oppenheimer
UPDATE movie SET trailer_url = 'https://www.youtube.com/embed/mqqft2x_Aa4' WHERE id = 8; -- The Batman
```
> **Note** : le jeu de données **ne contient pas d'utilisateur** et par conséquent **pas de réservations**.
> Vous pouvez les créer manuellement via le bouton `inscription` de la barre de navigation puis faire des réservations qui apparaîtront
> ensuite dans 'Mes Reservations'
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

| Route                | Accès    | Description                                |
|----------------------|----------|--------------------------------------------|
| `/index`             | Tous     | Page d'accueil avec recherche              |
| `/cinemas`           | Tous     | Liste des cinémas (filtre ville + mot-clé) |
| `/movies?cinemaId=x` | Tous     | Films d'un cinéma                          |
| `/seances?movieId=x` | Tous     | Séances d'un film                          |
| `/login`             | Tous     | Formulaire de connexion                    |
| `/register`          | tous     | Formulaire d'inscription                   |
| `/logout`            | Connecté | Déconnexion                                |
| `/my-reservations`   | Connecté | Historique des réservations                |
| `/reserve` (POST)    | Connecté | réserver une ou plusieurs places           |
| `/admin/towns`       | Admin    | Gestion des villes                         |
| `/admin/cinemas`     | Admin    | Gestion des cinéma                         |
| `/admin/movies`      | Admin    | Gestion des films                          |
| `/admin/seances`     | Admin    | Gestion des séances                        |

---

## Structure du projet

```text
Distopia/
├── src/
│   └── main/
│       ├── java/fr/fms/Distopia/
│       │   ├── DistopiaApplication.java          # Point d'entré
│       │   ├── config/
│       │   │   └── SecurityConfig.java           # BCrypt + Security filter chain
│       │   ├── dao/                              # JPA Repository
│       │   │   ├── TownRepository.java            
│       │   │   ├── CinemaRepository.java
│       │   │   ├── MovieRepository.java
│       │   │   ├── SeanceRepository.java          
│       │   │   ├── UserRepository.java
│       │   │   └── ReservationRepository.java
│       │   ├── entities/                         # Entités JPA 
│       │   │   ├── Town.java                     
│       │   │   ├── Cinema.java                   
│       │   │   ├── Movie.java                                     
│       │   │   ├── Seance.java                   
│       │   │   ├── User.java                     
│       │   │   ├── Reservation.java                   
│       │   │   └── Role.java                      
│       │   ├── service/                           # Logique métier
│       │   │   ├── TownService.java              
│       │   │   ├── CinemaService.java            
│       │   │   ├── MovieService.java             
│       │   │   ├── SeanceService.java            
│       │   │   ├── UserService.java              
│       │   │   ├── ReservationService.java       
│       │   ├── exceptions/                        # exceptions personnalisées
│       │   │   ├── SecurityFilterException.java 
│       │   │   └── NoSeatsAvailableException.java 
│       │   ├── utils/
│       │   │   └── SessionUtils.java          
│       │   └── web/                               # Controleurs
│       │       ├── IndexController.java
│       │       ├── TownController.java
│       │       ├── CinemaController.java
│       │       ├── MovieController.java
│       │       ├── SeanceController.java
│       │       ├── UserController.java
│       │       └── ReservationController.java
│       └── resources/
│           ├── templates/
│           │   ├── layout.html                   # Navbar + footer communs
│           │   ├── index.html                    # Accueil
│           │   ├── cinemas.html                  # Recherche cinémas
│           │   ├── movies.html                   # Films d'un cinéma
│           │   ├── seances.html                  # Séances + réservation
│           │   ├── my-reservations.html          # Historique réservations
│           │   ├── login.html                    # Formulaire de connexion
│           │   ├── register.html                 # Formulaire d'inscription
│           │   ├── admin-towns.html              # Formulaire admin villes
│           │   ├── admin-cinemas.html            # Formulaire admin Cinémas
│           │   ├── admin-movies.html             # Formulaire admin films
│           │   └── admin-seances.html            # Formulaire admin séances
│           ├── static/
│           │   └── css/
│           │       └── bootstrap.min.css
│           └── application.properties
├── pom.xml
└── README.md
```
# Licence

Ce projet est réalisé dans le cadre d'un exercice d'évaluation Spring Boot / JPA / Thymeleaf et est destiné à des fins pédagogiques uniquement.
© 2026 [Jonathan Maier](https://github.com/john7440)
