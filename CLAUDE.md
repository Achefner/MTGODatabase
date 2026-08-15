# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

Requires JDK 21 on PATH (`javac -version`) — a newer JDK on PATH produces class files the runtime can't load ("Unsupported class file major version").

```bash
mvn quarkus:dev                                # dev mode, H2 in-memory (http://localhost:8080)
mvn quarkus:dev -Dquarkus.profile=local         # dev mode, local postgres (see Profiles below)
mvn test                                        # run tests
mvn package                                     # build uber-jar
```

Single test class:
```bash
mvn test -Dtest=CardResourceTest
```

## Profiles

- **dev** (default, no flag needed) — H2 in-memory, schema `drop-and-create` on every start. No setup required.
- **local** — postgres via docker-compose (`docker compose up -d`, db/user/pass `mtgodb`/`mtgo`/`mtgo` on 5432), schema `update`. Run with `-Dquarkus.profile=local` or `QUARKUS_PROFILE=local`.
- **prod** — postgres, same creds/URL as `local` but expects an externally-managed instance (no docker-compose).

All three write through `PostgresCardRepository` — same JPA entity, only the datasource differs.

## Architecture

Hexagonal architecture — domain is isolated from framework and infrastructure concerns.

```
be.dontletit/
├── domain/          # pure domain: Card, CardType (no framework annotations)
├── api/             # inbound port: CardApiPort (getCardByName, getRandomCard, getRandomCards)
├── spi/             # outbound port: CardSpiPort (getAllCards), CardData record
├── application/     # CardApiAdapter: implements CardApiPort, injects CardSpiPort
└── infrastructure/
    ├── rest/        # CardController (/cards), ScryfallImportController (/admin/scryfall)
    └── data/
        ├── PostgresCardRepository   # implements CardSpiPort — JPA-backed, active in dev/local/prod
        ├── InMemoryCardRepository   # @DefaultBean fallback sample data, only used if no other CardSpiPort bean exists
        ├── entity/                 # CardEntity (JPA), StringListConverter
        └── scryfall/                # bulk import: ScryfallImportService, ScryfallCardMapper, JSON DTOs
```

**Dependency rule**: domain and ports have zero framework/infra imports. Only `application/` and `infrastructure/` touch Quarkus/Jakarta annotations.

**Storage model**: one `CardEntity` row per Scryfall printing (id = Scryfall printing UUID), not per unique card name. `PostgresCardRepository.getAllCards()` groups rows by name and unions each printing's set into `CardData.sets()` — don't reintroduce a raw `FROM CardEntity` query without that grouping step or `sets` collapses back to one element per card.

**Unknown Scryfall types**: `CardApiAdapter.toDomain` filters `data.types()` against `CardType` before `valueOf`, silently dropping tokens that aren't real gameplay types (e.g. Scryfall's `Card` for Art Series objects, or newer types like `Attraction`/`Kindred`/`Stickers` not yet in the enum). Don't swap that back to a bare `.map(CardType::valueOf)` — it throws on live bulk data.

## Endpoints

- `GET /cards/random` — returns a random card
- `GET /cards/random/{count}` — returns `count` random cards
- `GET /cards/{name}` — returns card by name (case-insensitive), 404 if missing
- `POST /admin/scryfall/import` — downloads Scryfall's default-cards bulk data and imports into the DB

## Scryfall import notes

- Bulk metadata (`GET /bulk-data/default-cards`) and the actual download both require an explicit `Accept` header — Scryfall 400s Java's `HttpClient` without one (curl sends `Accept: */*` by default, which is why a plain curl check can look fine while the app's request fails).
- The download itself is gzip-compressed JSONL (`jsonl_download_uri` field, one JSON object per line) — not a plain JSON array behind `download_uri` (that field no longer exists in Scryfall's response).

## Stack

- Quarkus 3.21, Java 21
- `quarkus-rest` + `quarkus-rest-jackson` for REST/JSON
- `quarkus-hibernate-orm-panache` + `quarkus-jdbc-postgresql` / `quarkus-jdbc-h2` for persistence
- CDI (`@ApplicationScoped`) for injection
