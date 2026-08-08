# Yoga Studio

Application web de gestion de séances de yoga, composée d’un backend Spring Boot et d’un frontend Angular.

## Prérequis

- Java 21 et Maven 3.9 ou version ultérieure ;
- Node.js et npm ;
- Docker Desktop, avec Docker Compose disponible ;
- un navigateur web.

Les versions et informations spécifiques à chaque partie sont également détaillées dans les README du [backend](back/README.md) et du [frontend](front/README.md).

## Versions utilisées

| Technologie | Version |
| --- | --- |
| Spring Boot | 3.5.16 |
| Angular | 19.2.15 |
| MySQL | 9.7.1 |

## Installer et lancer l’application

Depuis la racine de ce dépôt, préparer puis démarrer le backend :

```bash
cd back
mvn spring-boot:run
```

Le backend est disponible sur `http://localhost:8080`. Spring Boot démarre le conteneur MySQL défini dans `back/compose.yaml` ; Docker doit donc être lancé avant la commande.

> [!IMPORTANT]
> La configuration locale du backend nécessite un fichier `back/.env`, non versionné.
> Créer ce fichier à partir du bloc suivant, puis renseigner les valeurs adaptées à l’environnement :

```dotenv
DB_NAME=
DB_USER=
DB_PASSWORD=
DB_ROOT_PASSWORD=
DB_HOST=
DB_PORT=
TOKEN_SECRET=
```

> [!CAUTION]
> Ne jamais ajouter ce fichier ou ses secrets au dépôt.

Dans un second terminal, installer les dépendances puis lancer le frontend :

```bash
cd front
npm install
npm run start
```

Ouvrir ensuite `http://localhost:4200`. Le proxy Angular redirige les appels `/api` vers le backend local sur le port `8080`.

L’application permet notamment de créer un compte, de s’authentifier, de consulter les séances et, selon le rôle, de les créer, modifier ou supprimer.

## Lancer les tests

### Backend

Depuis `back/` :

```bash
# Tests unitaires (Surefire)
mvn test

# Tests unitaires et d’intégration (Failsafe), avec contrôle de couverture
mvn clean verify
```

Les tests d’intégration utilisent Testcontainers : Docker doit être démarré. La commande `mvn clean verify` supprime d’abord les rapports Maven précédents, puis applique un seuil de 80 % sur les indicateurs JaCoCo (instructions, branches, lignes, complexité, méthodes et classes). Le ratio de tests d’intégration, fixé à 30 %, est affiché dans le rapport mais ne bloque pas le build.

### Frontend

Depuis `front/` :

```bash
# Tous les tests Jest
npm run test

# Tests unitaires uniquement
npm run test:unit

# Tests d’intégration Angular uniquement
npm run test:integration

# Tests end-to-end Cypress
npm run cypress:run
```

Les fichiers `*.spec.ts` regroupent les tests unitaires, les fichiers `*.integration.spec.ts` les tests d’intégration Angular et les fichiers `*.cy.ts` les scénarios end-to-end Cypress.

## Générer et consulter les rapports de couverture

Les rapports de couverture sont conservés dans le dépôt, sous `coverage-reports/`, afin de pouvoir les consulter sans les régénérer.

### Backend : JaCoCo

Depuis `back/`, exécuter :

```bash
mvn clean verify
```

La commande met à jour les rapports suivants :

- global : [`coverage-reports/back-jacoco/index.html`](coverage-reports/back-jacoco/index.html) ;
- tests unitaires : [`coverage-reports/back-jacoco-unit/index.html`](coverage-reports/back-jacoco-unit/index.html) ;
- tests d’intégration : [`coverage-reports/back-jacoco-integration/index.html`](coverage-reports/back-jacoco-integration/index.html).

### Frontend : Jest

Depuis `front/`, exécuter :

```bash
npm run test:coverage
```

Cette commande génère et met à jour :

- le rapport global : [`coverage-reports/front-jest/lcov-report/index.html`](coverage-reports/front-jest/lcov-report/index.html) ;
- le rapport des tests unitaires : [`coverage-reports/front-jest-unit/lcov-report/index.html`](coverage-reports/front-jest-unit/lcov-report/index.html) ;
- le rapport des tests d’intégration : [`coverage-reports/front-jest-integration/lcov-report/index.html`](coverage-reports/front-jest-integration/lcov-report/index.html).

Le rapport global exige au minimum 80 % de couverture pour les statements, branches, functions et lines. Il affiche également le ratio de tests d’intégration, dont l’objectif de suivi est de 30 % sans rendre la commande bloquante.

### End-to-end : Cypress

Depuis `front/`, lancer les tests E2E avec l’instrumentation de couverture :

```bash
npm run e2e:ci
```

`npm run e2e:ci` exécute les tests E2E et génère directement le rapport de couverture. Pour exécuter les E2E sans produire la couverture, utiliser `npm run cypress:run` (ou `npx cypress run`).

Le rapport E2E est disponible dans [`coverage-reports/e2e/lcov-report/index.html`](coverage-reports/e2e/lcov-report/index.html).

Pour visualiser un rapport, ouvrir son fichier `index.html` dans un navigateur. Les liens de navigation internes aux rapports permettent ensuite de parcourir le détail par package ou par fichier.
