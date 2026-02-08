# 🧩 Kotlin Service Template — Spring Boot + Docker + Postgres

This project is a **production-style template** I like to use for building modern my Kotlin backend services.

It also acts as an way for future employers to measure my skills.

---

## 🏗 Stack

| Layer | Technology                                  |
|------|---------------------------------------------|
| Language | Kotlin (Java 21)                            |
| Framework | Spring Boot                                 |
| API Style | REST (Spring Web MVC)                       |
| Database | PostgreSQL                                  |
| ORM | Spring Data JPA / Hibernate                 |
| Containerization | Docker (multi-stage build + custom runtime) |
| Orchestration | Docker Compose                              |
| Health Monitoring | Spring Boot Actuator                        |

---

## 🚀 Running the Project

### 1️⃣ Requirements

- Docker
- Docker Compose

No local Java or PostgreSQL installation is required.

---

### 2️⃣ Environment Configuration

Copy the `.env.example` file in the project root, and rename the new one to `.env` :

---

### 3️⃣ Start the system
docker compose up --build

This starts:

| Service | Description | URL |
|---------|-------------|-----|
| Backend API | Spring Boot service | http://localhost:8080 |
| PostgreSQL | Database | http://localhost:5432 |

---

### 4️⃣ Health Check


Verify the service is ready:

```
GET http://localhost:8080/actuator/health
```


Expected response:

```
{ "status": "UP" }
```
