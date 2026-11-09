# 🛒 Online Supermarket System (Chrono BBS)

> Course design project — a full-stack e-commerce system built with Spring Boot and Vue 3.

> English | [简体中文](./README.md)

---

## Table of Contents

- [Project Overview](#project-overview)
- [Team](#team)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Documentation Index](#documentation-index)
- [UML Diagrams](#uml-diagrams)
- [Core Modules](#core-modules)
- [Security Design](#security-design)
- [Project Schedule](#project-schedule)
- [Contributing](#contributing)
- [License](#license)

---

## Project Overview

Chrono BBS is a B2B2C multi-merchant online supermarket platform. Guests can browse products, registered users can shop and place orders, merchants can manage their products and orders, and administrators can manage the whole platform and view sales reports.

**Original assignment description:**

> Users access the online supermarket through a browser. The system displays all products by category so that users can gradually find what they need. It also provides keyword search. While browsing the product catalog, users can click a product to view its details and price. If satisfied, they can temporarily add it to a shopping cart and remove it at any time. After selecting products, users proceed to payment by entering a credit card number; the system contacts the corresponding bank payment system to start payment. Finally, the system should provide administrators with query interfaces and various reports that summarize product sales.

---

## Team

| Role | Size | Responsibilities |
|------|------|------------------|
| Backend developer | 1 | User/authentication/authorization, product/category/SKU, cart/order/payment/refund, sales reports, search integration, WebSocket |
| Frontend developer | 1 | All Vue 3 pages, Axios wrapper, WebSocket |
| Tester | 1 | Test case design, unit tests, integration tests, E2E tests, defect tracking |

- **Development cycle:** 6 weeks (about 1.5 months)
- **Demo environment:** local laptop, middleware orchestrated by Docker

---

## Tech Stack

| Layer | Technology | Version / Notes |
|-------|------------|-----------------|
| **Backend framework** | Spring Boot | 3.4.5 |
| **Languages** | Java / TypeScript | Java 21 + TS 5.x |
| **ORM** | MyBatis | 3.0.4 |
| **Database** | MySQL | 8.0 |
| **Cache** | Redis | 7.x |
| **Search engine** | Elasticsearch | 8.10.x |
| **Frontend framework** | Vue 3 + Vite | Composition API |
| **UI library** | Element Plus | Recommended for Vue 3 |
| **State management** | Pinia | Recommended for Vue 3 |
| **Build tool** | Vite | HMR in development + Rollup production build |
| **Authentication** | JWT + Redis + Spring AOP | RBAC authorization |
| **Real-time messaging** | WebSocket (JSR 356) | User–merchant chat |
| **API documentation** | springdoc-openapi + knife4j-ui | Auto-generated API docs |
| **Object storage** | Aliyun OSS + Thumbnailator | Image upload with 800px compression |
| **Containerization** | Docker + Docker Compose | One-command middleware setup |
| **Deployment** | Nginx | Frontend/backend separation + reverse proxy |

---

## Project Structure

```
chrono-bbs/
├── chrono-bbs-server/                # Backend (Maven multi-module)
│   ├── common/                       # Shared module (Result / exceptions / utilities)
│   ├── module-mapper/                # Data access (Entity + Mapper + XML)
│   ├── module-service/               # Business logic (Service interfaces + Impl + DTO)
│   ├── module-api/                   # API layer (Controller + VO + interceptors)
│   ├── module-start/                 # Bootstrap module (configuration + scheduled tasks)
│   └── pom.xml                       # Parent POM
│
├── chrono-bbs-client/                # Frontend (Vue 3 + Vite + TS + Element Plus)
│
├── docs/                             # 📄 Documentation
│   ├── zh/                           # Chinese documentation (requirements/design/API/test/HMI)
│   └── en/                           # English documentation
│
├── uml/                              # 📐 UML diagrams
│   ├── uml-usecase.puml              # Use case diagram
│   ├── uml-component.puml            # Component diagram
│   ├── uml-er.puml                   # ER diagram
│   ├── uml-sequence.puml             # Sequence diagram
│   └── uml-activity.puml             # Activity diagram
│
├── docker/                           # 🐳 Docker orchestration
│   └── docker-compose.yml            # MySQL + Redis + ES
│
├── initial/                          # 🗄 Database initialization scripts
│   ├── schema.sql                    # DDL
│   └── data.sql                      # Seed data
│
├── deploy/                           # 🚀 Deployment configuration
│   └── nginx.conf                    # Nginx static site + API proxy
│
├── AGENTS.md                         # 🤖 AI assistant guidelines
├── CHANGELOG.md                      # 📝 Changelog
├── README.md                         # Chinese README
└── README.en.md                      # English README
```

---

## Quick Start

### Requirements

| Software | Version |
|----------|---------|
| JDK | 21+ |
| Node.js | 18+ |
| Docker Desktop | Latest |
| Maven | 3.8+ |

### 1. Start middleware

```bash
cd docker
docker compose up -d
```

This starts MySQL, Redis, and Elasticsearch. On first creation of the MySQL data volume, `initial/schema.sql` and `initial/data.sql` are executed automatically (creating the `chrono_bbs_dev` database, 20 tables, and seed data). No manual import is required.

> Ports: MySQL `3307` (to avoid conflicting with a local 3306), Redis `6379`, Elasticsearch `9200`.
> If you change the initialization scripts, run `docker compose down -v` to remove the data volume and re-run them.

### 2. Initialize the database (optional, manual)

If you use a local MySQL instead of Docker:

```bash
mysql -u root -p < initial/schema.sql
mysql -u root -p < initial/data.sql
```

The scripts are in `initial/` and create the `chrono_bbs_dev` database. The default administrator is `admin` / `admin123`.

### 3. Start the backend

```bash
cd chrono-bbs-server
mvn clean install -DskipTests
cd module-start
mvn spring-boot:run
```

The backend runs at `http://localhost:8080`. Knife4j API documentation is available at `http://localhost:8080/doc.html`.

### 4. Start the frontend

```bash
cd chrono-bbs-client
npm install
npm run dev
```

The frontend runs at `http://localhost:5173`.

### 5. Default test account

| Role | Username | Password |
|------|----------|----------|
| Administrator | `admin` | `admin123` |

> Regular users can register through the frontend.

### 6. Production deployment (Nginx)

```bash
cd chrono-bbs-client
npm run build
# Copy the contents of dist/ to the Nginx static root
```

Place `deploy/nginx.conf` in the Nginx configuration directory and reload Nginx. The configuration includes:

- History-mode fallback: `try_files $uri $uri/ /index.html`;
- `/api/` reverse proxy to backend port `8080`;
- `client_max_body_size 10m` for product image uploads;
- Long-term caching for `/assets/` and gzip compression.

> If Nginx runs in a container, replace `127.0.0.1` in `proxy_pass` with the backend service name or `host.docker.internal`.

---

## Documentation Index

> Chinese originals are in `docs/zh/`; English translations are in `docs/en/`. Each document has a language switcher at the top.

| Document | Description |
|----------|-------------|
| [requirements-analysis.md](./docs/en/requirements-analysis.md) | Requirements analysis — business background, core rules, 48 functional requirements (F-01–F-48), priorities, business flows, and data requirements |
| [object-oriented-development.md](./docs/en/object-oriented-development.md) | Object-oriented development — requirements and use cases, domain analysis, responsibility assignment, architecture, and detailed design steps |
| [feasibility-analysis.md](./docs/en/feasibility-analysis.md) | Feasibility analysis — technical, schedule, economic, and operational perspectives |
| [server-design.md](./docs/en/server-design.md) | Server high-level design — Maven modules, authentication, exception handling |
| [client-design.md](./docs/en/client-design.md) | Client high-level design — directory structure, routing, state management |
| [db-design.md](./docs/en/db-design.md) | Database design — tables, columns, and indexes |
| [detailed-design.md](./docs/en/detailed-design.md) | Detailed design — Mermaid flowcharts, sequence diagrams, and deployment architecture |
| [api-doc.md](./docs/en/api-doc.md) | API documentation — endpoint list and request/response examples |
| [test-specification.md](./docs/en/test-specification.md) | Test specification — unit, integration, and E2E requirements |
| [hmi-design.md](./docs/en/hmi-design.md) | HMI design — page prototypes and interactions |
| [git-commit-convention.md](./docs/en/git-commit-convention.md) | Git commit convention — Conventional Commits and branching model |
| [CHANGELOG.md](./CHANGELOG.md) | Changelog — release history |
| [AGENTS.md](./AGENTS.md) | AI assistant guidelines — coding and commit constraints |

## UML Diagrams

| Diagram | Tool | Description |
|---------|------|-------------|
| [uml-usecase.puml](./uml/uml-usecase.puml) | PlantUML | Use case diagram — guest/user/merchant/administrator |
| [uml-component.puml](./uml/uml-component.puml) | PlantUML | Component diagram — Maven modules and external dependencies |
| [uml-er.puml](./uml/uml-er.puml) | PlantUML | ER diagram — relationships among 20 tables |
| [uml-sequence.puml](./uml/uml-sequence.puml) | PlantUML | Sequence diagram — registration/login, shopping/order, payment, WebSocket |
| [uml-activity.puml](./uml/uml-activity.puml) | PlantUML | Activity diagram — order status flow, merchant onboarding, refund/return |

---

## Core Modules

| Module | Description |
|--------|-------------|
| 👤 User | Registration, login (JWT + Redis), profile, shipping address management |
| 🏪 Product | Three-level category browsing, product details, multi-SKU support |
| 🔍 Search | Elasticsearch full-text search, suggestions, multi-condition filtering and sorting |
| 🛒 Cart | Authenticated cart stored in the database, quantity updates, stock validation |
| 📦 Order | Order placement (anti-duplicate token + distributed lock), order status flow |
| 💳 Payment | Simulated payment (Luhn card validation + QR code payment) |
| 🎟 Coupon | Admin coupon CRUD, user claiming (Redis anti-oversell), my coupons, checkout discount calculation |
| 🔄 Refund/Return | Apply → review → return shipment → refund → stock restoration |
| 🏬 Merchant console | Onboarding review, product/stock management, shipping, refund review, sales statistics |
| 🛡 Admin console | User management, merchant review, category management, global reports (6 types) |
| 💬 Real-time chat | WebSocket chat between users and merchants (low priority) |

---

## Security Design

- Passwords stored with BCrypt hashing
- JWT + Redis dual validation (supports forced logout)
- Spring AOP + `@RequireRole` RBAC authorization
- Anti-duplicate order token (Redis atomic DEL)
- Optimistic locking for stock deduction (`WHERE stock >= ?`)
- Idempotent payment (deduplicated by `order_id`)
- MyBatis parameterized queries to prevent SQL injection

---

## Project Schedule

| Phase | Time | Tasks |
|-------|------|-------|
| Design + environment setup | Weeks 1–2 | Requirements confirmation, database design, API definition, Docker environment, search engine research |
| Core development | Weeks 3–5 | User/product/cart/order/payment CRUD, merchant/admin consoles, search integration |
| Wrap-up + defense | Week 6 | Sales reports, refund/return, integration testing, bug fixing, defense presentation |

---

## Contributing

This is a course design team project. Please read the following conventions before development:

- [Git commit convention](./docs/en/git-commit-convention.md) — commit message format and branching model
- [AGENTS.md](./AGENTS.md) — AI assistant guidelines (coding style and commit constraints)

Workflow: pull from `dev` → create a `feature/*` branch → develop → open a PR to `dev` → merge to `main` when stable.

---

## License

This project is a course design assignment. **All Rights Reserved.** Commercial use or redistribution without authorization is prohibited.
