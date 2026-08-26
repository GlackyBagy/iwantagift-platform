# iwantagift Platform

A microservices platform for creating, sharing, and managing wishlists.

## Services

| Service | Description | Status |
|---|---|---|
| `auth-service` | Registration, login, credential/password management, email verification, OAuth2 authorization server, JWT issuing | Ready |
| `wishlist-service` | CRUD for wishlists and wishes | Ready |
| `profile-service` | User profiles and avatars (stored via S3) | Ready |
| `mail-service` | Transactional email (verification, password reset, credential changes) via AWS SES, driven by Kafka events | Ready |
| `ui-service` | Web frontend / BFF, currently server-rendered with Thymeleaf | Ready, being rewritten in React |
| `productprice-service` | Item pricing and related logic | In development |
| `monitoring-service` | Logging, metrics, observability | Planned |

## Features

- **Wishlists** — create wishlists, add wishes, and share them with others via a link
- **Accounts** — sign up, sign in, verify your email, and reset a forgotten password
- **Profiles** — a personal page with an avatar and public wishlists
- **Notifications by email** — account and security events (verification, password reset, credential changes) trigger transactional emails

## Tech Stack

- **Backend:** Java 21, Spring Boot, Spring Data JPA, Flyway, Spring Security (OAuth2 Authorization/Resource Server), Apache Kafka
- **Frontend:** Thymeleaf, migrating to React
- **Data & Storage:** PostgreSQL, Redis, AWS S3, AWS SES
- **Infrastructure:** Docker Compose, GitHub Actions CI/CD (build, test, publish to GHCR, deploy), Caddy as reverse proxy / TLS termination

## Roadmap

- Migrate the frontend from Thymeleaf to React
- Adopt Spring Cloud for service discovery, configuration, and API gateway concerns
- Gifting system with an intermediary and attachable messages
- Crowdfunding for individual items or whole wishlists, with group contributions
- Monitoring, logging, and metrics
