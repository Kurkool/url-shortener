# URL Shortener Service

A production-ready backend API for shortening URLs, managing user-owned links, and redirecting short codes to their original destinations. The service is built with Spring Boot, secured with JWT-based authentication, and persists data in PostgreSQL.

## Features

- Register and log in with email and password (BCrypt-hashed).
- Obtain JWTs to access protected endpoints.
- Shorten long URLs to unique short codes.
- Redirect short codes via `GET /r/{code}`.
- List and deactivate previously generated URLs.
- Comprehensive validation, exception handling, and integration tests.

## Tech Stack

- Java 17, Spring Boot 3.3
- Spring Data JPA, Hibernate
- Spring Security with JWT (jjwt)
- PostgreSQL (H2 in-memory database for tests)
- Maven Wrapper (`mvnw`)

## Getting Started

### Prerequisites

- Java 17+
- Docker (optional, for running PostgreSQL locally)

### Configure PostgreSQL

Create a PostgreSQL database:

```bash
docker run --name url-shortener-db \
  -e POSTGRES_DB=url_shortener \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

Adjust credentials via environment variables if needed.

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/url_shortener` | JDBC URL for PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `APP_BASE_URL` | `http://localhost:8080` | Base URL used when building short links |
| `APP_JWT_SECRET` | `bXlzdXBlcnNlY3JldGtleW15c3VwZXJzZWNyZXRrZXk=` | Base64-encoded signing secret (replace in production) |
| `APP_JWT_EXPIRATION` | `3600` | JWT expiration in seconds |

### Run the Application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Run Tests

```bash
./mvnw test
```

Tests run against an in-memory H2 database using the `test` profile.

## API Endpoints

### Authentication

#### Register

`POST /api/register`

Request:

```http
POST /api/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Secret123!"
}
```

Response `200 OK`:

```json
{
  "token": "<jwt-token>"
}
```

#### Login

`POST /api/login`

Request:

```http
POST /api/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Secret123!"
}
```

Response `200 OK`:

```json
{
  "token": "<jwt-token>"
}
```

### URL Management (requires `Authorization: Bearer <token>`)

#### Shorten URL

`POST /api/shorten`

```http
POST /api/shorten
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "originalUrl": "https://example.com/some/very/long/link"
}
```

Response `200 OK`:

```json
{
  "id": "b4c3c788-9b1c-4f6e-9e4f-3b2a6b7d5e23",
  "originalUrl": "https://example.com/some/very/long/link",
  "shortUrl": "http://localhost:8080/r/abc1234",
  "active": true,
  "createdAt": "2025-11-12T16:21:45.123456Z"
}
```

#### List URLs

`GET /api/urls`

Response `200 OK`:

```json
[
  {
    "id": "b4c3c788-9b1c-4f6e-9e4f-3b2a6b7d5e23",
    "originalUrl": "https://example.com/some/very/long/link",
    "shortUrl": "http://localhost:8080/r/abc1234",
    "active": true,
    "createdAt": "2025-11-12T16:21:45.123456Z"
  }
]
```

#### Deactivate URL

`DELETE /api/urls/{id}`

Response `204 No Content`

### Redirect

`GET /r/{code}` — Redirects to the stored `originalUrl` with an HTTP 302 response and `Location` header.

## Error Handling

Errors return structured JSON:

```json
{
  "timestamp": "2025-11-12T16:22:12.345Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid URL format",
  "path": "/api/shorten"
}
```

## Security Notes

- Replace the default JWT secret before production.
- Consider configuring HTTPS and CORS restrictions for production deployments.
- Passwords are hashed with BCrypt using Spring Security.

