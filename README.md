# 🎁 Wishlist Platform



A microservices-based platform for creating, sharing, and managing wishlists in a secure and convenient way.

---

## 🚀 Overview

This platform allows users to create wishlists, share them with others, and receive gifts without exposing sensitive personal information such as their address.

It is especially useful for:
- content creators (bloggers, streamers)
- groups of friends
- collaborative gift funding

---

## 🏗️ Microservices

The system is designed as a set of independent services:

- **wishlist-service** — core service for managing wishlists (CRUD) ✅
- **productprice-service** — handles item pricing and related logic
- **auth-service** — authentication & authorization (Spring Security, OAuth2) 🚧
- **monitoring-service** — logging, metrics, and system monitoring 🚧

> Additional services may be added as the system evolves.

---

## 💡 Features

### Wishlist functionality
- Create public and private wishlists
- Manage items within wishlists
- Share wishlists with others

### Gifting system
- Send gifts via the platform as an intermediary
- Attach messages to gifts

### Crowdfunding
- Collect money for specific items or entire wishlists
- Support group contributions

### Privacy & Security
- Protect user personal data by ensuring sensitive information (e.g., addresses) is never exposed and is securely stored using encryption
- Secure authentication and authorization (in progress)

---

## 🛠️ Tech Stack

**Backend:**
- Java
- Spring Boot
- Spring Data JPA
- Spring Security (in progress)
- OAuth2 (planned)

**Frontend:**
- Thymeleaf

**Infrastructure:**
- Docker (planned)
- REST APIs

**Database:**
- PostgreSQL
- Redis

**Message brokers:**
- Kafka

---

## 📦 Current Status

- ✅ MVP for Wishlist CRUD microservice implemented
- 🚧 Authentication service in development
- 🚧 Frontend (UI) in development
- 🚧 Monitoring service planned
