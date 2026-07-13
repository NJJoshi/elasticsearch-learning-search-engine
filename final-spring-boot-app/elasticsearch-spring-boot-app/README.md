# Elasticsearch Spring Boot App

A Spring Boot 4 application demonstrating a full-text, faceted "business search" experience backed by Elasticsearch — including relevance-tuned search (fuzzy matching, field boosting, filters, geo-distance), aggregation-based facets, and completion-suggester based autocomplete. Includes a minimal Bootstrap-based single-page UI to exercise the API.

## Features

- **Full-text business search** (`GET /api/search`) with:
  - Multi-field relevance search across name, category, offerings, address, and description, with per-field boosting and fuzzy matching (typo tolerance).
  - Filters: state (exact match), minimum rating, offerings, and geo-distance radius from a lat/lon point.
  - Category boosting via a `should` clause on the exact (keyword) category field.
  - Pagination.
  - Facets: aggregated counts of `offerings` values for the current result set, usable to build filter UIs.
  - Total hit count and query execution time in the response.
- **Autocomplete / "search-as-you-type" suggestions** (`GET /api/suggestions`) using Elasticsearch's `completion` suggester, with fuzzy matching and duplicate skipping.
- **RFC 9457 `ProblemDetail` error responses** for validation errors (e.g. missing required query/prefix parameters).
- **Demo UI** (`src/main/resources/static/index.html`) — a single static HTML page (Bootstrap 5 + vanilla JS) that calls the two REST endpoints to provide search-as-you-type, star ratings, filter sidebar (rating, distance, state, offerings facets), and result cards.
- **Integration tests** that spin up a real Elasticsearch instance via Testcontainers, seed it with fixture data, and assert on the actual HTTP responses — no mocking of Elasticsearch behavior.

## Architecture

```
Browser (static/index.html)
        │  HTTP (GET /api/search, /api/suggestions)
        ▼
BusinessSearchController (REST layer)
        │
        ├──► SearchService ─────► NativeQueryBuilder.toSearchQuery() ─────► ElasticsearchOperations ──► "businesses" index
        │                              (built from QueryRule/QueryRules)
        │
        └──► SuggestionService ─► NativeQueryBuilder.toSuggestQuery() ─────► ElasticsearchOperations ──► "suggestions" index
                                       (ElasticsearchUtil.buildCompletionSuggester)
```

**Layers**

- **`controller`** — `BusinessSearchController` exposes the two REST endpoints. Request query parameters are bound directly to record-based parameter objects (`SearchRequestParameters`, `SuggestionRequestParameters`), which self-validate in their compact constructors.
- **`service`** — `SearchService` and `SuggestionService` orchestrate query building, execution via Spring Data Elasticsearch's `ElasticsearchOperations`, and response shaping.
- **`util`** — the query-construction layer:
  - `NativeQueryBuilder` assembles a Spring Data Elasticsearch `NativeQuery` (search or suggestion) from validated request parameters.
  - `QueryRule` / `QueryRules` implement a small rules-engine pattern: each rule pairs a `Predicate<SearchRequestParameters>` (should this clause apply?) with a `Function<SearchRequestParameters, Query>` (how to build it), letting `NativeQueryBuilder` compose `bool` query `filter` / `must` / `should` clauses declaratively and only include clauses for parameters actually supplied.
  - `ElasticsearchUtil` contains low-level Elasticsearch Java Client DSL builders (term query, range query, geo-distance query, multi-match query, terms aggregation, completion suggester) reused by `QueryRules`.
  - `Constants` centralizes index names (`IndexCoordinates`), field names, and fuzziness settings.
- **`dto`** — immutable Java records for the search domain: `Business` (the indexed document / response entity), request parameter records, and response shape records (`SearchResponse`, `Pagination`, `Facet`, `FaceItem`).
- **`advice` / `exceptions`** — `ApplicationExceptionHandler` (`@ControllerAdvice`) maps `BadRequestException` to an HTTP 400 `ProblemDetail` response.

### Query construction detail (`GET /api/search`)

`NativeQueryBuilder.toSearchQuery` builds a Boolean query out of three rule groups:

| Clause type | Rule | Applies when | Elasticsearch query |
|---|---|---|---|
| `filter` | `STATE_QUERY` | `state` param present | term query on `state` (keyword) |
| `filter` | `RATING_QUERY` | `rating` param present | range query `avg_rating >= rating` |
| `filter` | `DISTANCE_QUERY` | `distance`, `latitude`, `longitude` all present | `geo_distance` query on `location` |
| `filter` | `OFFERINGS_QUERY` | `offerings` param present | term query on `offerings.raw` (keyword) |
| `must` | `SEARCH_QUERY` | `query` present (always, since it's required) | `multi_match` (type `most_fields`, operator `AND`, fuzziness 1, prefix length 2) over boosted fields: `name^2.0`, `category^1.5`, `offerings^1.5`, `address^1.2`, `description` |
| `should` | `CATEGORY_QUERY` | `query` present | term query on `category.raw` boosted `5.0`, so exact category matches (e.g. searching "coffee" matching a "Coffee Shop" category) rank higher |

An `offerings-term-aggregate` terms aggregation on `offerings.raw` is always attached, producing the `facets` returned in the response for building filter UIs (e.g. an "Offerings" checkbox list with counts).

### Suggestion construction detail (`GET /api/suggestions`)

`NativeQueryBuilder.toSuggestQuery` builds a suggest-only query (`withMaxResults(0)`, source excluded) using a `completion` suggester (`ElasticsearchUtil.buildCompletionSuggester`) against the `search_term` field of the `suggestions` index, with fuzziness 1 / prefix length 2 and duplicate skipping. `SuggestionService` flattens the nested suggester response (`suggestion → entries → options`) into a flat `List<String>`.

## Tech Stack

- **Java 25**, **Spring Boot 4.1.0**
- **Spring Data Elasticsearch** (`spring-boot-starter-data-elasticsearch`) using the modern Elasticsearch Java API Client (`co.elastic.clients`) — no legacy `RestHighLevelClient` / `ElasticsearchRestTemplate`.
- **Spring Web MVC** (`spring-boot-starter-webmvc`)
- **Elasticsearch 9.0.2** (used in tests via Testcontainers; compatible version expected at runtime)
- **Testcontainers** (`testcontainers-elasticsearch`, `spring-boot-testcontainers`, JUnit 5 Jupiter integration) — tests boot a real, ephemeral Elasticsearch 9.0.2 container (security disabled) via `@ServiceConnection`, so no external Elasticsearch instance is needed to run the test suite.
- **Bootstrap 5** (CDN) + vanilla JS for the demo UI — no frontend build step.

## Project Structure

```
src/main/java/.../elasticsearchspringbootapp/
├── ElasticsearchSpringBootAppApplication.java   # @SpringBootApplication entry point
├── controller/BusinessSearchController.java     # REST endpoints
├── service/
│   ├── SearchService.java                       # /api/search orchestration + response building
│   └── SuggestionService.java                   # /api/suggestions orchestration
├── util/
│   ├── NativeQueryBuilder.java                  # builds NativeQuery for search & suggest
│   ├── QueryRule.java / QueryRules.java          # declarative predicate→query rules
│   ├── ElasticsearchUtil.java                    # low-level ES Java Client DSL builders
│   └── Constants.java                            # index names, field names, fuzziness config
├── dto/                                          # Business, SearchRequestParameters,
│                                                  # SuggestionRequestParameters, SearchResponse,
│                                                  # Pagination, Facet, FaceItem
├── exceptions/BadRequestException.java
└── advice/ApplicationExceptionHandler.java       # maps exceptions to RFC 9457 ProblemDetail

src/main/resources/
├── application.properties                        # spring.elasticsearch.uris (commented out — set for local runs)
└── static/index.html                              # demo single-page UI

src/test/java/.../elasticsearchspringbootapp/
├── AbstractTest.java                              # shared test base: boots app + loads JSON fixtures
├── TestcontainersConfiguration.java               # @ServiceConnection Elasticsearch 9.0.2 container
├── TestElasticsearchSpringBootAppApplication.java # dev-only launcher: runs the app with the
│                                                   # Testcontainers Elasticsearch auto-attached
├── SearchTest.java                                # integration tests for /api/search
└── SuggestionTest.java                            # integration tests for /api/suggestions

src/test/resources/test-data/
├── business-index-setting.json / business-index-mapping.json / business-data.json
└── suggestion-index-mapping.json / suggestion-data.json
```

## Data Model

### `businesses` index

Backing record: `dto.Business`.

| Field | ES type | Notes |
|---|---|---|
| `name` | `text` | boosted `2.0` in search |
| `description` | `text` | custom analyzer (`custom_description_analyzer`: standard tokenizer + lowercase + stop-word filter) |
| `address` | `text` | boosted `1.2` in search |
| `state` | `keyword` | exact-match filter |
| `location` | `geo_point` | used for geo-distance filter |
| `category` | `text` with `category.raw` (`keyword`) sub-field | `text` boosted `1.5` in multi-match; `.raw` used for exact-match `should` boost `5.0` |
| `offerings` | `text` with `offerings.raw` (`keyword`) sub-field | `text` boosted `1.5`; `.raw` used for filter + facet aggregation |
| `avg_rating` | `float` | mapped from JSON field `avg_rating` to `Business.rating` |
| `num_of_reviews` | `integer` | mapped from JSON field `num_of_reviews` to `Business.reviewsCount` |
| `url` | `keyword`, `index: false` | stored but not searchable |

### `suggestions` index

Single field `search_term` of type `completion`, used purely for the completion suggester (autocomplete).

## API Reference

### `GET /api/search`

Query parameters (bound to `SearchRequestParameters`):

| Param | Required | Description |
|---|---|---|
| `query` | **yes** | free-text search term (400 error if blank) |
| `state` | no | exact US state name filter |
| `rating` | no | minimum `avg_rating` (inclusive) |
| `offerings` | no | exact offering filter |
| `distance`, `latitude`, `longitude` | no (all three needed together) | geo-distance filter, e.g. `distance=25mi` |
| `page` | no | default `0` |
| `size` | no | default `10` |

Response (`SearchResponse`): `results` (list of `Business`), `facets` (list of `Facet` → `FaceItem[]`, currently `offerings-term-aggregate`), `pagination` (`page`, `size`, `totalElements`, `totalPages`), `timeTaken` (ms).

Example:
```
GET /api/search?query=coffee&state=Washington&rating=4&page=0&size=10
```

### `GET /api/suggestions`

Query parameters (bound to `SuggestionRequestParameters`):

| Param | Required | Description |
|---|---|---|
| `prefix` | **yes** | prefix to autocomplete (400 error if blank) |
| `limit` | no | default `10` |

Response: `List<String>` of matched terms (fuzzy-tolerant, duplicates skipped).

Example:
```
GET /api/suggestions?prefix=cof&limit=5
→ ["coffee"]
```

### Error responses

Validation failures (empty `query` / `prefix`) return HTTP 400 with an RFC 9457 `ProblemDetail` body, e.g.:
```json
{ "status": 400, "detail": "Query parameter is required" }
```

## Running Locally

1. Start an Elasticsearch instance (e.g. Docker) and set the connection in `src/main/resources/application.properties`:
   ```properties
   spring.application.name=elasticsearch-spring-boot-app
   spring.elasticsearch.uris=http://localhost:9200
   ```
2. Create the `businesses` and `suggestions` indices with the settings/mappings under `src/test/resources/test-data/` (used as fixtures in tests) and load some documents.
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Open `src/main/resources/static/index.html` in a browser (served at `http://localhost:8080/index.html` once the app is running), or call the API directly.

## Running Tests

```bash
./mvnw test
```

Tests use Testcontainers to launch a disposable Elasticsearch 9.0.2 container automatically (`TestcontainersConfiguration`) — no local Elasticsearch setup is required. `AbstractTest` loads index settings/mappings/seed data from `src/test/resources/test-data/*.json` before each test class runs, and assertions are made against real HTTP responses from the running Spring Boot app (`TestRestTemplate`, random port).

- `SearchTest` — parameterized success cases (plain query, rating filter, state filter, offerings filter, geo-distance filter with in/out-of-range cases, pagination, fuzzy-match typo tolerance) and failure cases (blank query → 400).
- `SuggestionTest` — parameterized success cases (prefix match, limit, fuzzy prefix tolerance and its boundary) and failure cases (blank prefix → 400).
