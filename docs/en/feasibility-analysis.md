# Online Supermarket System — Feasibility Analysis Document

> [简体中文](../zh/feasibility-analysis.md) | English

---

## I. Introduction

### 1.1 Project Overview

| Item | Content |
|------|---------|
| Project Name | Online Supermarket System |
| Project Type | Course Design |
| Development Cycle | About 1.5 months |
| Team Size | 3 people (frontend and backend separated, collaborative development) |
| Technology Selection | Decided autonomously by the team |

### 1.2 Analysis Dimensions

This document conducts a comprehensive analysis from four dimensions: **technical feasibility**, **schedule feasibility**, **economic feasibility**, and **operational feasibility**, to assess whether the project can be successfully completed under the given constraints.

---

## II. Technical Feasibility Analysis

### 2.1 Technology Stack Overview

| Layer | Technology Selection | Team Familiarity | Learning Cost |
|------|----------|------------|----------|
| Backend Framework | Java 21 + Spring Boot 3.x | ★★★★☆ Familiar | Low |
| ORM | MyBatis 3.x | ★★★★☆ Familiar | Low |
| Database | MySQL 8.x | ★★★★☆ Familiar | Low |
| Frontend Framework | Vue 3 + TypeScript + Vite | ★★★☆☆ Moderate | Medium |
| UI Component Library | Element Plus | ★★★☆☆ Moderate | Low |
| State Management | Pinia | ★★★☆☆ Moderate | Low |
| Cache | Redis 7.x | ★★★☆☆ Moderate | Medium |
| Search Engine | Elasticsearch 7.x/8.x | ★★☆☆☆ Unfamiliar | ⚠️ High |
| Instant Messaging | WebSocket (backend @OnOpen / frontend native API) | ★★☆☆☆ Unfamiliar | ⚠️ Medium |
| Containerization | Docker + Docker Compose | ★★☆☆☆ Unfamiliar | ⚠️ Medium |
| Deployment | Nginx | ★★★☆☆ Moderate | Low |
| Authentication & Authorization | JWT + Redis + service-layer RBAC | ★★★☆☆ Moderate | Medium |

### 2.2 Key Technical Risk Analysis

#### Risk 1: Elasticsearch (⚠️ High Risk)

| Risk Item | Analysis |
|--------|------|
| Risk Description | The team is unfamiliar with Elasticsearch and needs to learn deployment, Chinese word segmentation, index design, and DSL queries from scratch |
| Dependencies | Docker ES image deployment → Chinese analyzer (IK Analyzer) → index mapping design → data synchronization (MySQL → ES) → frontend DSL queries |
| Scope of Impact | Search functionality (high priority F-10) and search suggestion/autocomplete functionality |
| Learning Curve | ES has many concepts (index, mapping, analyzer, DSL); an estimated 5-7 days of learning + practice is required |
| **Mitigation Measures** | 1. Prioritize rapid deployment of ES and Kibana (visual debugging) using Docker<br>2. Complete basic product CRUD first, then integrate ES incrementally<br>3. Search suggestions (autocomplete) can be a degraded goal, with simple `LIKE` queries as a fallback<br>4. Team division: assign 1 person to be specifically responsible for ES research and integration |

#### Risk 2: WebSocket Instant Messaging (⚠️ Medium Risk)

| Risk Item | Analysis |
|--------|------|
| Risk Description | The backend uses JSR 356 annotations (@OnOpen / @OnMessage), and the frontend uses the native WebSocket API; connection management, reconnection, and message dispatch need to be handled |
| Scope of Impact | Instant messaging functionality (low priority F-46 ~ F-47), does not affect the core shopping flow |
| **Mitigation Measures** | 1. Set instant messaging as low priority and develop it after core functionality is complete<br>2. Encapsulate a `useWebSocket` composable on the frontend to uniformly manage the connection lifecycle<br>3. Use `ConcurrentHashMap` on the backend to manage user connection sessions<br>4. Keep the message format simple (JSON); no additional protocol needs to be introduced |

#### Risk 3: Docker Container Orchestration (⚠️ Medium Risk)

| Risk Item | Analysis |
|--------|------|
| Risk Description | The team is unfamiliar with Docker and needs to learn Docker Compose to orchestrate three services (MySQL + Redis + ES) simultaneously |
| Scope of Impact | Development environment and demo environment setup |
| **Mitigation Measures** | 1. Develop using local installations first (install MySQL and Redis directly) to ensure business development is not blocked<br>2. Write Docker Compose files in the middle stage to start all middleware with one command<br>3. Docker Compose orchestration requires only 1 `docker-compose.yml` file, and the learning cost is manageable (1-2 days) |

#### Risk 4: Redis Cache and Distributed Lock (⚠️ Low-Medium Risk)

| Risk Item | Analysis |
|--------|------|
| Risk Description | Redis needs to implement login state caching, anti-duplicate submission tokens, distributed locks (order placement), product sales counting, etc. |
| Scope of Impact | Multiple high-priority features such as authentication, order placement, and sales statistics |
| Learning Curve | Basic single-node Redis operations are simple; distributed locks have a certain learning cost |
| **Mitigation Measures** | 1. First use simple `SET key value EX seconds` to implement login state and anti-duplicate tokens<br>2. Use Redisson or hand-written `SETNX` + Lua scripts for distributed locks, referring to mature solutions online<br>3. Use `Redis INCRBY` for sales counting and asynchronously synchronize back to MySQL, which is simple and reliable |

### 2.3 Technical Feasibility Conclusion

| Conclusion | Description |
|------|------|
| ✅ **Overall feasible** | The core technology stack (Spring Boot + Vue + MySQL) is a mature and widely used combination with abundant community resources |
| ⚠️ **Risky but controllable** | ES and WebSocket have learning costs, but both have mitigation measures, and WebSocket has been downgraded to low priority. As long as learning time is allocated reasonably, core feature delivery is not affected |
| 📋 **Key items must be done early** | Research and integration of ES should start as early as possible (weeks 1-2) and not be postponed to the later stage |

---

## III. Schedule Feasibility Analysis

### 3.1 Time Constraints

| Constraint Item | Description |
|--------|------|
| Total Cycle | About 6 weeks (1.5 months) |
| Team Size | 3 people |
| Available Work Hours | Assuming each person can invest about 15-20 hours per week, total work hours ≈ 270 ~ 360 person-hours |

### 3.2 Development Phase Breakdown

| Phase | Time | Main Tasks | Deliverables |
|------|------|----------|--------|
| **Weeks 1-2**: Design + Environment Setup | 2 weeks | Requirements confirmation, database design, API definition, Docker environment setup, ES research, frontend framework initialization | Database table creation SQL, API documentation, project skeleton |
| **Weeks 3-5**: Core Feature Development | 3 weeks | User/product/category/cart/order/payment CRUD, merchant backend, administrator backend, search (ES) | Runnable MVP |
| **Week 6**: Wrap-up + Documentation | 1 week | Sales reports, refunds and returns, integration testing, bug fixing, defense documents/PPT | Complete system + defense materials |

### 3.3 Suggested Division of Work

| Role | Headcount | Responsible Modules |
|------|------|----------|
| Backend Developer A | 1 person | User/authentication/authorization, product/category/SKU, ES integration |
| Backend Developer B | 1 person | Cart/order/payment/refund, sales reports, WebSocket |
| Frontend Developer | 1 person | All Vue 3 frontend pages: mall layout + merchant backend + administrator backend, Axios encapsulation, WebSocket frontend |

> Note: If someone progresses quickly, they can provide cross-support. For ES integration, backend A and the frontend are advised to collaborate on integration testing.

### 3.4 Schedule Risk Assessment

| Risk | Probability | Impact | Response |
|------|------|------|------|
| ES learning takes longer than expected | Medium | Search feature delayed | Get basic search working first; advanced features such as autocomplete can be added later |
| Docker environment issues | Medium | Development blocked | Allow direct development with locally installed MySQL/Redis first |
| Frontend workload exceeds expectations | Medium | Some pages delayed | Complete core pages first (mall shopping flow); streamline backend management pages |
| Pressure from other final-term courses | High | Time compressed in the last 2 weeks | Complete at least 90% of the code before week 5; only do wrap-up in week 6 |

### 3.5 Schedule Feasibility Conclusion

| Conclusion | Description |
|------|------|
| ✅ **Time is basically sufficient** | Completing a course-design-level project in 6 weeks with a 3-person team is feasible |
| ⚠️ **Scope must be strictly controlled** | Low-priority features (comments, instant messaging, search history) must be decisively cut to avoid affecting core features |
| 📋 **Milestones are key** | The core shopping loop (browse → cart → place order → pay) must be completed by the end of week 4; otherwise subsequent risks are extremely high |

---

## IV. Economic Feasibility Analysis

### 4.1 Cost Analysis

| Cost Item | Cost | Description |
|--------|------|------|
| Development Tools | ¥0 | IntelliJ IDEA Community / VS Code (free) |
| Database | ¥0 | MySQL Community Edition (free and open source) |
| Cache | ¥0 | Redis Community Edition (free and open source) |
| Search Engine | ¥0 | Elasticsearch free edition |
| Containerization | ¥0 | Docker Desktop (free for individuals) |
| Cloud Services | ¥0 | Runs locally on personal laptops; no need to purchase cloud servers |
| Third-party Services | ¥0 | Simulated payment; no integration with real payment interfaces |
| **Total Cost** | **¥0** | All free and open-source technologies are used |

### 4.2 Economic Benefits

| Benefit Item | Description |
|--------|------|
| Course Grade | Complete the course design and earn credits |
| Technical Accumulation | Master full-stack development skills with Spring Boot + Vue + ES + Redis + Docker |
| Project Experience | Can serve as a resume project to enhance employment competitiveness |

### 4.3 Economic Feasibility Conclusion

| Conclusion | Description |
|------|------|
| ✅ **Fully feasible** | Zero economic cost; all technology stacks are free and open source, with no external dependency costs |

---

## V. Operational Feasibility Analysis

### 5.1 Operating Environment

| Environment | Configuration Requirements | Description |
|------|----------|------|
| Operating System | Windows 10/11 | Personal laptop |
| Memory | ≥ 16 GB | ES consumes a relatively large amount of memory by default (can be reduced with `-Xms512m -Xmx512m`) |
| Disk | ≥ 20 GB available | Docker images + project code + database data |
| Software | Docker Desktop, JDK 21, Node.js 18+, Maven 3.x | Development and build tools |

### 5.2 Demo Feasibility

| Aspect | Analysis |
|------|------|
| Demo Method | Run all services on a personal laptop (Spring Boot + Vue/Nginx + MySQL + Redis + ES) |
| Number of Services | 5 services running simultaneously (1 backend + 1 frontend + 1 database + 1 cache + 1 search engine) |
| Laptop Load | Orchestrated uniformly with Docker Compose; ES memory limited to 512MB; overall memory usage about 3-4 GB, bearable on a 16GB laptop |
| Demo Stability | Purely local network, no network dependency, high stability |

### 5.3 User Adaptability

| Aspect | Analysis |
|------|------|
| Interface Friendliness | Uses the Element Plus component library; interaction patterns conform to domestic user habits |
| Learning Cost | Mimics the layout of mainstream e-commerce websites (JD/Taobao); users need no additional learning |
| Browser Compatibility | Compatible with the latest versions of Chrome / Edge (for course demos) |

### 5.4 Operational Feasibility Conclusion

| Conclusion | Description |
|------|------|
| ✅ **Fully feasible** | Personal laptops can carry all services; the interface is intuitive and easy to use; no external network dependency is required |

---

## VI. Comprehensive Feasibility and Recommendations

### 6.1 Overall Conclusion

| Dimension | Conclusion | Confidence |
|------|------|--------|
| Technical Feasibility | ✅ Feasible (risky but controllable) | 85% |
| Schedule Feasibility | ✅ Feasible (scope must be strictly controlled) | 80% |
| Economic Feasibility | ✅ Fully feasible | 100% |
| Operational Feasibility | ✅ Fully feasible | 95% |

**Comprehensive Evaluation**: The project is feasible in all four dimensions: technical, schedule, economic, and operational. It is recommended to proceed with project initiation and development.

### 6.2 Key Success Factors

1. **Start ES learning and integration as early as possible** (weeks 1-2); this is the biggest technical risk point
2. **Strictly follow priorities**; decisively cut low-priority features (comments, instant messaging) when time is insufficient
3. **Achieve the shopping loop by the end of week 4** (browse → cart → place order → pay); this is the most important milestone
4. **Develop frontend and backend in parallel**; the frontend first develops pages with Mock data, then quickly integrates once the backend APIs are ready
5. **One-command startup with Docker Compose** to avoid the "it works on my machine" problem

### 6.3 Summary of Major Risks and Mitigation Measures

| Risk | Level | Mitigation Measures |
|------|------|----------|
| High ES learning cost | ⚠️ High | Dedicated research in weeks 1-2; get basic search working first; advanced features deferred |
| Unfamiliar with WebSocket | ⚠️ Medium | Downgrade to low priority; develop after core features are complete |
| Unfamiliar with Docker | ⚠️ Medium | Allow direct local installation of middleware first; add Docker Compose in the middle stage |
| Final-term time compression | ⚠️ Medium | Complete 90% of the code before week 5; decisively cut low-priority features |
| Large frontend development volume | ⚠️ Medium | Simplify styles on admin pages; prioritize the mall shopping flow |
| Redis distributed lock | ⚠️ Low | Refer to mature solutions; abundant community resources |
