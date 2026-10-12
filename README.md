# Grocy

Grocy is a lightweight web application for **managing shared grocery lists within households**. The project is primarily a learning project focused on gaining hands-on experience with **Spring Boot** and DevOps practices, while keeping the application simple, collaborative, and fast.

## Features

* Manage grocery lists (create, update, delete)
* Multiple users per household
* Collaborative list usage (lists can be opened and edited by multiple users)
* Authentication and basic role handling (OWNER/EDITOR/VIEWER)
* Active household per user
* Invite user to edit/view lists
* Select default list

## Tech Stack

### Frontend

* Vue 3
* Vite
* Pinia (state management)
* TypeScript
* Element Plus

### Backend

* Spring Boot
* Spring Security (JWT)
* Spring Data JPA
* PostgreSQL

### Infrastructure

* PostgreSQL
* Environment-based configuration

## Getting Started

### Prerequisites

* Node.js (>= 18)
* Java 17+
* PostgreSQL database

### Run Backend

```bash
docker compose up -d db
.\mvnw.cmd "-Dspring-boot.run.profiles=local" spring-boot:run
```

Copy `.env.example` to `.env` and replace every `CHANGE_ME`. `.env` is gitignored.
For local development, set `PORT=8081`,
`SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:3300/grocy`,
`SPRING_DATASOURCE_USERNAME=grocy_admin` and `APP_FRONTEND_URL=http://localhost:5173`.
Use the existing database password and a random JWT secret of at least 32 bytes.
The backend runs on `http://localhost:8081` with the `local` profile.

### Run Frontend

```bash
cd src/vueapp/grocy-frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173` and proxies `/api` requests to `http://localhost:8081`.

## Architecture Notes

* Services encapsulate API logic
* Stores (Pinia) manage global application state
* Backend exposes a REST API under `/api`
* Authentication via JWT; the token is stored in the browser and sent as `Authorization: Bearer`
* Production notes: `docs/PRODUCTION_CHECKLIST.md`, `docs/TOMS.md`, `docs/DATA_PROCESSING_INVENTORY.md`

Placeholders only: `.env.example`. Do not put real passwords or JWT values into versioned files.

## Roadmap / Ideas

* Indicate when a list is currently opened by other users
* Improved mobile usability
* Product history and suggestions
* Household-based statistics

## Contributing

Issues, pull requests, and ideas are welcome.

## License

This project is currently private / work in progress. A license may be added later.
