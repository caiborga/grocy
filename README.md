# Grocy

Grocy is a lightweight web application for **managing shared grocery lists within households**. The project is primarily a learning project focused on gaining hands-on experience with **Spring Boot**, while keeping the application simple, collaborative, and fast.

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

* PostgreSQL (e.g. Neon)
* Hosting via Koyeb
* Environment-based configuration

## Getting Started

### Prerequisites

* Node.js (>= 18)
* Java 17+
* PostgreSQL database

### Run Backend

```bash
cd backend
./mvnw spring-boot:run
```

Required environment variables (example):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/db
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password
```

### Run Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173` and proxies API requests to `/api`.

## Architecture Notes

* Services encapsulate API logic
* Stores (Pinia) manage global application state
* Backend exposes a REST API under `/api`
* Authentication via JWT stored in local storage

## Roadmap / Ideas

* Indicate when a list is currently opened by other users
* Improved mobile usability
* Product history and suggestions
* Household-based statistics

## Contributing

Issues, pull requests, and ideas are welcome.

## License

This project is currently private / work in progress. A license may be added later.
