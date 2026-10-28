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
- **local** — postgres via docker-compose (`docker compose up -d`, db/user/pass `mtgodb`/`mtgo`/`mtgo` on host port **5433** — 5432 is taken by a system postgres on this machine), schema `update`. This is the profile to use day to day: cards and collections survive restarts. Run with `-Dquarkus.profile=local` or `QUARKUS_PROFILE=local`.
- **prod** — postgres, same creds/URL as `local` but expects an externally-managed instance (no docker-compose).

All three write through `PostgresCardRepository` — same JPA entity, only the datasource differs.

## Architecture

Hexagonal architecture — domain is isolated from framework and infrastructure concerns.

```
be.dontletit/
├── domain/          # pure domain: Card, CardType; CardCollection, CollectionEntry, CollectionType (DECK/CUBE/MISC)
├── api/             # inbound ports: CardApiPort, CollectionApiPort (+ request/response records, UnknownCardsException)
├── spi/             # outbound ports: CardSpiPort (getAllCards), CollectionSpiPort (save/findAll/findById/delete) + data records
├── application/     # CardApiAdapter, CollectionApiAdapter (name resolution, validation), CardMapping, DecklistFormat (text parse/format)
└── infrastructure/
    ├── rest/        # CardController (/cards), CollectionController (/collections), ScryfallImportController (/admin/scryfall)
    └── data/
        ├── PostgresCardRepository        # implements CardSpiPort — JPA-backed, active in dev/local/prod
        ├── PostgresCollectionRepository  # implements CollectionSpiPort — CollectionEntity + element collection of entries
        ├── InMemoryCardRepository        # @DefaultBean fallback sample data, only used if no other CardSpiPort bean exists
        ├── entity/                       # CardEntity, CollectionEntity, CollectionEntryEmbeddable, StringListConverter
        └── scryfall/                     # bulk import: ScryfallImportService, ScryfallCardMapper, JSON DTOs
```

**Dependency rule**: domain and ports have zero framework/infra imports. Only `application/` and `infrastructure/` touch Quarkus/Jakarta annotations.

**Storage model**: one `CardEntity` row per Scryfall printing (id = Scryfall printing UUID), not per unique card name. `PostgresCardRepository.getAllCards()` groups rows by name and unions each printing's set into `CardData.sets()` — don't reintroduce a raw `FROM CardEntity` query without that grouping step or `sets` collapses back to one element per card. The canonical printing of a group (its `id`, `originalPrintedSet` + name/type, `firstPrintedAt`, `rarity`, `imgUrl`) is the earliest-released **non-promo** printing (promo flag first, then `releasedAt` nulls last, then id); promo printings (prerelease, judge, buy-a-box — Scryfall `promo: true`) only win when the card was never printed anywhere else. Without that rule prerelease promos, dated before the main set, become the "first set".

**Unknown Scryfall types**: `CardApiAdapter.toDomain` filters `data.types()` against `CardType` before `valueOf`, silently dropping tokens that aren't real gameplay types (e.g. Scryfall's `Card` for Art Series objects, or newer types like `Attraction`/`Kindred`/`Stickers` not yet in the enum). Don't swap that back to a bare `.map(CardType::valueOf)` — it throws on live bulk data.

## Endpoints

- `GET /cards/names` — returns every distinct card name, sorted (~38k entries)
- `GET /cards/sets` — sets in which at least one card was first printed: code, name, Scryfall `set_type`, release date (earliest first printing), card count; chronological
- `GET /cards/random` — returns a random card
- `GET /cards/random/{count}` — returns `count` random cards
- `GET /cards/{name}` — returns card by name (case-insensitive), 404 if missing
- `POST /admin/scryfall/import` — downloads Scryfall's default-cards bulk data and imports into the DB

### Collections (decks, cubes, misc)

- `POST /collections` — JSON `{name, type: DECK|CUBE|MISC, entries: [{name, quantity?}]}` → 201 + Location
- `POST /collections/import?name=&type=` — body `text/plain` decklist ("4 Lightning Bolt" / "Lightning Bolt" per line, `#` and `//` comments, section headers and Arena "(SET) 123" suffixes ignored) → 201
- `GET /collections` — summaries (id, name, type, cardCount)
- `GET /collections/{id}` — entries (canonical card name + quantity)
- `GET /collections/{id}/export` — `text/plain` decklist, one `N Name` line per entry (round-trips with import)
- `GET /collections/{id}/cards` — full `CardResponse` list expanded by quantity, in collection order
- `PUT /collections/{id}` — replace name, type and entries; `DELETE /collections/{id}` → 204
- Entry names are resolved case-insensitively against the card table, also by front-face name ("Brazen Borrower" → "Brazen Borrower // Petty Theft"); duplicates are merged. Unknown names → 400 `{"unknownCards": [...]}` and nothing is stored.
- Collections live in the same datasource as cards: with the default `dev` profile (H2 in-memory, drop-and-create) they vanish on restart — use `local`/`prod` (postgres) to keep them.

## CORS

`dev` and `local` profiles allow any origin (`quarkus.http.cors.*` in `application.properties`).
Browsers normally don't hit this service directly any more — front ends go through a bridge
service such as `../MTGOEnchantWordleService` — so this is only a convenience for ad-hoc testing.
`prod` has no CORS config.

## Scryfall import notes

- Objects whose `layout` is `art_series`, `token`, `double_faced_token` or `emblem` are skipped (`ScryfallCardMapper.isImportable`) — Art Series objects are named "X // X" and polluted the names list with thousands of fake cards. Reversible cards named "X // X" are stored as "X"; genuine two-face names ("Delver of Secrets // Insectile Aberration") are kept as Scryfall writes them.
- Multi-face cards are stored as the game sees them: `ScryfallCardMapper.frontFace` takes mana cost, type line, text, power/toughness and image from `card_faces[0]` for transform / modal DFC / adventure / flip / reversible layouts (Brazen Borrower // Petty Theft is a {1}{U}{U} Creature — Faerie Rogue). Split and aftermath cards keep Scryfall's combined top-level fields (rule 709.3), and their type line "Sorcery // Instant" is parsed face by face and merged without duplicates. Names keep both faces as Scryfall writes them.
- Tests bind `quarkus.http.test-port=8090` so `mvn test` works while the bridge service dev server holds :8081.

- Bulk metadata (`GET /bulk-data/default-cards`) and the actual download both require an explicit `Accept` header — Scryfall 400s Java's `HttpClient` without one (curl sends `Accept: */*` by default, which is why a plain curl check can look fine while the app's request fails).
- The download itself is gzip-compressed JSONL (`jsonl_download_uri` field, one JSON object per line) — not a plain JSON array behind `download_uri` (that field no longer exists in Scryfall's response).

## Stack

- Quarkus 3.21, Java 21
- `quarkus-rest` + `quarkus-rest-jackson` for REST/JSON
- `quarkus-hibernate-orm-panache` + `quarkus-jdbc-postgresql` / `quarkus-jdbc-h2` for persistence
- CDI (`@ApplicationScoped`) for injection
