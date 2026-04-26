# pocatcher

Small full-stack app: Spring Boot API, React (Vite) UI, and Redis for caching. Data comes from [PokeAPI](https://pokeapi.co/).

## API

Base path: **`/api/v1/pocatcher`**

| Method | Path | Purpose |
|--------|------|--------|
| GET | `/api/v1/pocatcher/pokemons` | Paginated catalog (`limit`, `offset` query params) |
| GET | `/api/v1/pocatcher/pokemons/{id}` | Pokémon details |

With the API running on port **8080**:

- **OpenAPI JSON:** http://localhost:8080/v3/api-docs  
- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **Health:** http://localhost:8080/actuator/health  

## Run with Docker

From the repo root:

```bash
docker compose up --build
```

- **UI:** http://localhost:4173  
- **API:** http://localhost:8080  
- **Redis:** localhost:6379  

Stop with `Ctrl+C` or `docker compose down`.

## Projects

| Directory       | Role                          |
|-----------------|--------------------------------|
| `pocatcher-api` | Spring Boot REST API          |
| `pocatcher-ui`  | React + TypeScript + Vite UI |

## Architecture

The **frontend** (`pocatcher-ui`) is a React and TypeScript SPA that loads lists and details from the backend over HTTP. The **backend** (`pocatcher-api`) is a Spring Boot application that exposes REST endpoints, applies validation and error handling, and loads Pokémon data from [PokeAPI](https://pokeapi.co/) when it needs fresh content. **Redis** backs Spring’s cache so catalog and detail responses live outside the JVM and are reused on subsequent requests instead of calling PokeAPI again every time.


## TODO (remaining work)

- **Security:** TODO: Harden API error responses (avoid exposing raw exception messages to clients) and review auth, CORS, and rate limiting for any public deployment.
- **Null safety (PokeAPI DTOs):** TODO: Add null-safe handling in catalog fetch and weakness resolution for nullable PokeAPI payloads (`list` pages, `damage_relations` / `double_damage_from`, and related nested fields).
- **Primary Pokémon fetch:** TODO: Apply the same HTTP/network error handling pattern to the main `/pokemon/{id}` fetch as used for species and generation.
- **RestClient failures:** TODO: Ensure timeouts, connection errors, and 5xx responses from PokeAPI are handled consistently wherever `RestClient` is used, not only `HttpClientErrorException`.
- **Catalog warm-up:** TODO: Move full catalog warm-up off the synchronous `ApplicationReadyEvent` path so application readiness and health checks are not blocked by a long PokeAPI sync.
- **Paginated catalog:** TODO: Avoid loading the entire cached catalog on every paginated list request; use a more paging-friendly cache or slice strategy as data grows.
- **Frontend tests:** TODO: Add a minimal automated UI test suite (e.g. Vitest + Testing Library) and run it in CI alongside lint and typecheck.
