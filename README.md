# 🚢 Ship Management System

[中文文档](./README_CN.md) | English

---

<div align="center">

![Vue](https://img.shields.io/badge/Vue-3.5.24-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?logo=spring)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.13.0-409EFF)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Redis](https://img.shields.io/badge/Redis-Latest-DC382D?logo=redis)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=java)
![License](https://img.shields.io/badge/License-MIT-green)

**A Modern Full-Stack Ship Management Solution**

[Online Demo](#) | [Frontend Docs](./ship-management-front/) | [Backend Docs](./ship-manage-backend/) | [API Docs](./船舶管理系统后端接口文档.md)

</div>

---

## 📋 Table of Contents

- [Introduction](#introduction)
- [Core Features](#core-features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Recent Updates](#recent-updates)

---

## Introduction

Ship Management System is a modern full-stack web application built with **Vue 3 + Spring Boot 3**, featuring a frontend-backend separated architecture. This system provides a complete digital solution for ship management, covering core business processes including ships, crew, voyages, maintenance, fuel records, and certificate management.

### 🎯 Design Goals

- **User-Friendly** - Intuitive UI to reduce learning curve
- **Comprehensive** - Full coverage of ship management workflows
- **Secure** - Multi-layer security protection for data safety
- **Extensible** - Modular design for easy feature expansion

---

## Core Features

### 🔧 Business Modules

#### 1. Ship Management
- ✅ Ship CRUD operations
- ✅ Ship classification (bulk carriers, container ships, etc.)
- ✅ Ship status tracking (in service/under maintenance/idle/sailing)
- ✅ Cover image upload (support Aliyun OSS/AWS S3)
- ✅ **Batch Delete** - Support batch deletion of ship records

#### 2. Crew Management
- ✅ Crew file management
- ✅ Crew-to-ship assignment
- ✅ Position management (Captain, Chief Officer, Chief Engineer, etc.)
- ✅ **Batch Delete** - Support batch deletion of crew records

#### 3. Voyage Management
- ✅ Voyage planning and execution tracking
- ✅ Departure and arrival port records
- ✅ Voyage status management (planned/in progress/completed)
- ✅ Time management
- ✅ **Batch Delete** - Support batch deletion of voyage records

#### 4. Maintenance
- ✅ Maintenance record management
- ✅ Maintenance type classification
- ✅ Cost recording and statistics
- ✅ **Batch Delete** - Support batch deletion of maintenance records

#### 5. Fuel Management
- ✅ Fuel consumption records
- ✅ Fuel type management (HFO, MDO, etc.)
- ✅ Cost analysis and statistics
- ✅ Supplier management
- ✅ **Batch Delete** - Support batch deletion of fuel records

#### 6. Certificate Management
- ✅ Ship certificate information management
- ✅ Certificate expiration auto-reminder
- ✅ Certificate status auto-update
- ✅ **Batch Delete** - Support batch deletion of certificate records

#### 7. Message Center
- ✅ Real-time message push (30-second polling)
- ✅ Multi-type messages (system/maintenance/voyage/certificate)
- ✅ Message reply functionality
- ✅ Unread message count

#### 8. Operation Logs
- ✅ AOP automatic recording of all operations
- ✅ Complete audit trail
- ✅ Admin viewing permissions

### 🔐 Security & Authentication

#### Authentication Mechanisms
- ✅ **JWT Token Authentication** - Redis-based session storage
- ✅ **JWT Key Configuration** - Support dynamic configuration for production
- ✅ **Role-Based Access Control** - ADMIN/USER dual role system
- ✅ **Email Verification Code** - Registration and password reset
- ✅ **Bot Protection** - Cloudflare Turnstile

#### Security Improvements ✅
- ✅ **Transaction Management** - @Transactional for critical operations
- ✅ **Global Exception Handling** - Unified error responses
- ✅ **File Upload Protection** - Null pointer validation
- ✅ **Layered Architecture** - Controller → Service → Mapper

### 📊 Data Management

- ✅ **Batch Delete** - All business modules support batch deletion
- ✅ **Excel/PDF Export** - One-click data export
- ✅ **Data Visualization** - ECharts for statistics
- ✅ **Pagination** - Efficient large data browsing
- ✅ **Advanced Search** - Multi-condition combination queries

---

## Tech Stack

### Frontend

| Technology | Version | Purpose | Features |
|------------|---------|---------|----------|
| **Vue** | 3.5.24 | Core framework | Composition API, excellent performance |
| **Vite** | 7.2.4 | Build tool | Fast cold start, HMR |
| **Element Plus** | 2.13.0 | UI component library | Beautiful, feature-rich |
| **Pinia** | 3.0.4 | State management | Type-safe, DevTools support |
| **Vue Router** | 4.6.4 | Routing | Dynamic routes, navigation guards |
| **Axios** | 1.13.2 | HTTP client | Interceptors, auto Token handling |
| **ECharts** | 6.0.0 | Data visualization | Rich chart types |
| **XLSX** | 0.18.5 | Excel processing | Frontend Excel export |

### Backend

| Technology | Version | Purpose | Features |
|------------|---------|---------|----------|
| **Spring Boot** | 3.3.0 | Core framework | Auto-configuration, rich ecosystem |
| **MyBatis-Plus** | 3.5.7 | ORM framework | Simplified CRUD, excellent performance |
| **MySQL** | 8.0+ | Primary database | Transaction support, stable performance |
| **Redis** | 6+ | Cache/Session | High performance, rich data structures |
| **JWT** | 4.4.0 | Authentication | Stateless, easy to scale |
| **Lombok** | 1.18.32 | Code simplification | Reduce boilerplate code |
| **Hutool** | 5.8.26 | Utility library | Rich Java utilities |
| **Spring Mail** | - | Email service | Email sending integration |

---

## Quick Start

### Prerequisites

- **Node.js** 18+
- **JDK** 17+
- **MySQL** 8.0+
- **Redis** 6+
- **Maven** 3.8+

### 1. Clone the Project

```bash
git clone https://github.com/your-repo/ship-management-system.git
cd ship-management-system
```

### 2. Database Configuration

Create MySQL database:

```sql
CREATE DATABASE ship_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Edit backend configuration file `ship-manage-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ship_management?serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
    
# JWT Configuration
jwt:
  secret: your-secret-key-here  # Use strong keys in production
  expiration: 86400000          # 24 hours (milliseconds)
  
# Email Configuration (optional)
resend:
  from-email: your-email@example.com
  api-key: your-api-key
```

### 3. Start Backend

```bash
cd ship-manage-backend

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

Backend service: `http://localhost:8088`

### 4. Start Frontend

```bash
cd ship-management-front

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend: `http://localhost:5173`

### 5. Access the System

Open browser and visit `http://localhost:5173`

Default admin account:
- Username: admin
- Password: admin123

---

## Project Structure

```
ship-management-system/
├── ship-manage-backend/          # Spring Boot Backend (Java)
│   ├── src/main/java/com/dhy/shipmanagebackend/
│   │   ├── controller/           # REST API Controllers (12)
│   │   │   ├── UserController.java
│   │   │   ├── AdminUserController.java
│   │   │   ├── ShipController.java
│   │   │   ├── CrewController.java
│   │   │   ├── VoyageController.java
│   │   │   ├── FuelRecordController.java
│   │   │   ├── ShipCertificateController.java
│   │   │   ├── MaintenanceController.java
│   │   │   ├── ShipCategoryController.java
│   │   │   ├── MessageController.java
│   │   │   ├── OperationLogController.java
│   │   │   └── FileUploadController.java
│   │   ├── service/              # Business Logic Layer
│   │   │   ├── impl/             # Service Implementations (10)
│   │   │   └── *Service.java     # Service Interfaces (11)
│   │   ├── mapper/               # MyBatis Mappers (10)
│   │   ├── entity/               # Entity Classes (11)
│   │   ├── dto/                  # DTO Classes
│   │   ├── utils/                # Utilities
│   │   │   ├── JwtUtil.java      # JWT Utility
│   │   │   ├── BcryptUtil.java   # Encryption Utility
│   │   │   └── ...
│   │   ├── annotation/           # Custom Annotations
│   │   ├── aspect/               # AOP Aspects (Operation Logs)
│   │   └── exception/            # Global Exception Handling
│   └── src/main/resources/
│       ├── application.yml       # Configuration
│       └── static/               # Static Resources
│
└── ship-management-front/        # Vue 3 Frontend
    ├── src/
    │   ├── api/                  # API封装
    │   ├── components/           # Reusable Components
    │   ├── layout/               # Layout Components
    │   ├── views/                # Page Components
    │   ├── stores/               # Pinia State Management
    │   ├── router/               # Routing Configuration
    │   └── utils/                # Utility Functions
    └── vite.config.js            # Vite Configuration
```

---

## API Documentation

### Batch Delete API

All business modules support batch deletion:

| Module | Endpoint | Method | Request Body | Notes |
|--------|----------|--------|--------------|-------|
| User Management | `/admin/user/batch` | DELETE | `{"ids": [1,2,3]}` | Auto-filters current user |
| Crew Management | `/crew/batch` | DELETE | `{"ids": [1,2,3]}` | |
| Ship Management | `/ship/batch` | DELETE | `{"ids": [1,2,3]}` | |
| Voyage Management | `/voyages/batch` | DELETE | `{"ids": [1,2,3]}` | |
| Fuel Records | `/fuel/batch` | DELETE | `{"ids": [1,2,3]}` | |
| Certificate Management | `/certificate/batch` | DELETE | `{"ids": [1,2,3]}` | |
| Maintenance | `/maintenance/batch` | DELETE | `{"ids": [1,2,3]}` | |
| Ship Categories | `/ship-categories/batch` | DELETE | `{"ids": [1,2,3]}` | |

### User Authentication API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/user/register` | POST | User registration (requires email verification) |
| `/user/login` | POST | Username/password login |
| `/user/login/email` | POST | Email verification code login |
| `/user/send-code` | POST | Send email verification code |
| `/user/info` | GET | Get current user info |
| `/user/avatar` | PUT | Update avatar |

---

## Recent Updates

### 2025-01-31 Important Updates ✅

1. **Security Improvements**
   - ✅ Fixed JWT key hardcoding, support application.yml configuration
   - ✅ Improved global exception handling, unified error response format
   - ✅ Added logging to prevent sensitive information leakage

2. **Architecture Optimization**
   - ✅ Refactored AdminUserController to layered architecture
   - ✅ Added @Transactional for data consistency

3. **Robustness Enhancement**
   - ✅ Fixed FileUploadController null pointer risk
   - ✅ Added parameter validation

4. **Feature Enhancement**
   - ✅ Implemented batch delete for all modules (8 modules)
   - ✅ Created unified DTO (BatchDeleteRequest)

---

## Configuration Guide

### JWT Security Configuration

```yaml
jwt:
  secret: your-secret-key-here-min-32-characters-long
  expiration: 86400000
```

⚠️ **Important**: Use complex keys in production, at least 32 characters long

### File Upload Configuration

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

---

## Development Roadmap

- [x] Basic CRUD functionality
- [x] JWT authentication and access control
- [x] Batch delete functionality
- [ ] Swagger API documentation
- [ ] Unit test coverage
- [ ] Docker containerization
- [ ] CI/CD pipeline

---

## Contributing

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

---

## License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  ⭐ If this project helps you, please give it a Star!<br>
  📧 Have questions or suggestions? Feel free to open an Issue or PR
</p>
