# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

MVU Platform is a web application for Musikverein Harmonie Urdorf (a Swiss music society). It manages concerts, repertoire, members, music scores, events, absences, documents, and supporter relations.

## Tech Stack

- **Backend**: Spring Boot 3.5 / Java 25
- **Frontend**: Vaadin Flow 24 with React components
- **Database**: PostgreSQL via jOOQ (code generation) + Flyway migrations
- **Build**: Maven (use `./mvnw`)

## Development Setup

**Start the database:**
```
docker compose -p mvu -f src/main/docker/postgres.yml down && docker compose -p mvu -f src/main/docker/postgres.yml up --build
```

**Run the app** (uses `local` Spring profile):
```
./mvnw spring-boot:run
```
Or use the IntelliJ run configuration `Application`.

**Regenerate jOOQ code** (run after DB schema changes, requires Docker):
```
mvn clean test-compile -Djooq-codegen-skip=false
```

## Build & Test Commands

| Task | Command |
|------|---------|
| Production build | `mvn clean package -Pproduction` |
| Run all tests | `./mvnw clean verify` |
| Check dependency updates | `mvn -U versions:display-property-updates` |

## Architecture

### Package Structure

Code lives under `src/main/java/ch/mvurdorf/platform/`. It is organized **by feature**, not by layer:

```
absenzen/     – absence & attendance tracking
ai/           – OpenAI integration
common/       – shared domain types
config/       – Spring @Configuration classes (cloud storage, MJML, etc.)
documents/    – document management
events/       – event views and services
home/         – public-facing home/concert/repertoire views
kontakte/     – contact management
konzerte/     – concert management
messaging/    – push notifications
noten/        – music scores management
repertoire/   – repertoire management
reminder/     – scheduled reminders
security/     – Firebase auth, OAuth2, AuthenticatedUser
service/      – cross-cutting services (mail, Firebase, storage, QR)
supporter/    – supporter management
ui/           – shared Vaadin components, MainLayout, LoginView
users/        – user management
utils/        – FormatUtil, DateUtil, BigDecimalUtil, DurationUtil
jooq/         – **auto-generated** jOOQ classes (do not edit manually)
```

### Within Each Feature Module

Each feature package typically contains:
- `*View.java` — Vaadin `@Route`-annotated view (the UI)
- `*Service.java` — business logic, uses jOOQ DSLContext directly
- `*Dto.java` / `*Endpoint.java` — data transfer and REST endpoints where needed

### Key Conventions (enforced by ArchUnit tests)

- **Constructor injection only** — no `@Autowired` field injection
- **Use `DateUtil.now()`** instead of `LocalDate.now()` (for testability)
- **Close jOOQ streams** — never use jOOQ `.stream()` without explicit closing
- **No deprecated API usage**

### Database

- Flyway migrations in `src/main/resources/db/migration/` (V1–V33+)
- jOOQ DSL is used directly in services; no Spring Data repositories
- Local DB: `postgresql://mvu:mvu@localhost:5432/mvu`

### Frontend

- Vaadin Flow handles routing and server-side rendering
- React components are used for interactive UI pieces
- Prettier config: single quotes, 120-char print width (`.prettierrc.json`)
- TypeScript strict mode enabled

### Infrastructure

- **Deployed on**: Heroku (`mvu` app)
- **Storage**: Google Cloud Storage (`mvurdorf-notenablage` bucket)
- **Auth**: Firebase Admin SDK
- **Email**: Spring Mail + MJML API for templated HTML emails
