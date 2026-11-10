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
| **Authentication** | JWT + Redis | Interceptor authentication + service-layer role/ownership checks |
| **Real-time messaging** | WebSocket (JSR 356) | User–merchant chat |
| **API documentation** | springdoc-openapi + knife4j-ui | Auto-generated API docs |
| **Image storage** | Local directory / Aliyun OSS + Thumbnailator | Local by default, OSS optional, 800px compression |
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
│   ├── schema.sql                    # DDL (latest schema)
│   ├── data.sql                      # Seed data
│   ├── migration-2026-11-09.sql      # Incremental migration (idempotent)
│   └── demo-data.sql                 # Demo merchants/products/orders/comments (idempotent)
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
| Maven | 3.8+ |
| Docker Desktop | Latest (Docker mode only) |

### 1. One-command startup (recommended)

Windows (cmd / PowerShell):

```bat
script\start-all.bat            :: Docker middleware (MySQL 3307)
script\start-all.bat --local    :: local middleware (MySQL 3306)
```

Bash / Git Bash / macOS / Linux:

```bash
./script/start-all.sh           # Docker middleware (MySQL 3307)
./script/start-all.sh --local   # local middleware (MySQL 3306)
```

The script starts middleware, backend, and frontend in order:

| Service | URL |
|---------|-----|
| Frontend | <http://localhost:5173> |
| Backend | <http://localhost:8080> |
| Knife4j docs | <http://localhost:8080/doc.html> |

> To serve the frontend from the Docker Nginx container instead of Vite, run `script\start-all.bat --docker-frontend --skip-build` (Bash: `./script/start-all.sh --docker-frontend --skip-build`); the frontend is then available at <http://localhost:3000>.
> On Windows, the backend and frontend each open in a new console window; close the window to stop the service.
> On Bash, the backend runs in the background with logs in `logs/backend.log`; `Ctrl+C` stops both backend and frontend.
> For more options (`--skip-build`, `--volumes`), see [script/README.md](script/README.md).
> **In Docker mode MySQL listens on host port 3307.** If you start the backend directly from an IDE, set `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/chrono_bbs_dev?...` explicitly or use `--local`; otherwise the backend connects to local port 3306 and its account/product/coupon data diverges from the Docker database (e.g. a claimed coupon does not appear under "My Coupons").

### 2. Stop middleware

| Operation | Windows | Bash |
|-----------|---------|------|
| Stop middleware | `script\stop-middleware.bat` | `./script/stop-middleware.sh` |

> Docker middleware is used by default (MySQL exposed on host port `3307`); add `--local` to use locally running MySQL / Redis / Elasticsearch (MySQL uses `3306` from `application-dev.yml`).

### 3. Deploy the frontend with Docker (optional)

A multi-stage frontend image (Node build + Nginx hosting) is provided; the backend still runs on host port `8080`:

```bash
cd docker
docker compose --profile frontend up -d --build nginx
```

| Service | URL |
|---------|-----|
| Frontend (Nginx container) | <http://localhost:3000> |

> Host port `3000` is used by default; override with `FRONTEND_PORT=3001`. Stop any local Nginx using 3000 first.
> The container reaches the backend through `host.docker.internal:8080`; if the backend is added to Compose, update the address in `deploy/nginx.docker.conf` to the service name.

### 4. Initialize a local database (optional)

When using `--local` for the first time, initialize the database:

```bash
mysql -u root -p < initial/schema.sql
mysql -u root -p < initial/data.sql
mysql -u root -p < initial/demo-data.sql
```

The scripts are in `initial/` and create the `chrono_bbs_dev` database. The default administrator is `admin` / `admin123`; demo merchants and users are listed below.

> To upgrade an existing database, run `mysql -u root -p chrono_bbs_dev < initial/migration-2026-11-09.sql` (idempotent); for demo data, also run `mysql -u root -p chrono_bbs_dev < initial/demo-data.sql` (idempotent).
> In Docker mode, the initialization scripts run in the order `schema.sql → data.sql → migration-2026-11-09.sql → demo-data.sql` on first startup. If you change them, run `./script/stop-middleware.sh --volumes` (Windows: `script\stop-middleware.bat --volumes`) to remove the data volume before restarting.
> In development, images are written to the backend's local `uploads/` directory (`upload.mode=local`) and no OSS configuration is required. To use object storage, copy `script/.env.example` to `.env`, fill in the keys and set `UPLOAD_MODE=oss`.

### 5. Default test account

| Role | Username | Password | Notes |
|------|----------|----------|-------|
| Administrator | `admin` | `admin123` | Initial administrator |
| Merchant | `merchant1` | `admin123` | Fresh fruit flagship store (approved) |
| Merchant | `merchant2` | `admin123` | Digital products store (approved) |
| Regular user | `user1` | `admin123` | Address, cart, orders, refund and comments included |

> Demo accounts are created by `initial/demo-data.sql`; regular users can also register through the frontend.

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

Before starting the backend in production, configure `DB_PASSWORD`, `JWT_SECRET` (a random string of at least 32 bytes) and OSS credentials (if OSS is enabled). The database passwords in `application-dev.yml`, `application-test.yml` and `docker-compose.yml` are for local development/testing only and must not be used in production.

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
- Service-layer role and ownership checks (admin role, merchant approval, product/order/refund ownership)
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