# feed-service

Aggregated event feed service for EventMaster. Combines following-based events and personalized recommendations into a single paginated feed. Stateless — no database.

Port: `8083`. Context path: `/feed-service`.

## Endpoint

```
GET /feed?type=FOLLOWING|RECOMMENDED|ALL&page=0&size=20
Authorization: Bearer <token>
```

### Feed Types

| Type | Source | Sort Order |
|------|--------|------------|
| `FOLLOWING` | Events from accounts the caller follows (fan-out via event-service) | Chronological by start time |
| `RECOMMENDED` | Events from recommendation-service, pre-ranked by score | Preserves ranker score order (descending) |
| `ALL` | Merge of FOLLOWING and RECOMMENDED, deduped by event ID | Chronological by start time |

For `FOLLOWING` and `ALL`, events are sorted chronologically so followed creators' events read as a timeline. For a pure `RECOMMENDED` feed the ranker's score ordering is preserved.

Each event in the response includes a `feedSource` field (`"FOLLOWING"` or `"RECOMMENDED"`).

## Cross-Service Communication

- **user-service**: Fetches the caller's following list (`/users/{username}/following`)
- **event-service**: Fetches upcoming events per followed creator in parallel
- **recommendation-service**: Fetches up to 100 ranked candidates

All calls degrade gracefully — a failure returns an empty slice for that source rather than failing the whole request.

## Stateless Design

No database in either local or Docker profiles. All feed data is assembled on-demand from upstream services.

## Running Locally

```bash
cd feed-service
mvn spring-boot:run
```

Available at `http://localhost:8083/feed-service`.

## Environment Variables

| Variable | Required | Default | Notes |
|----------|----------|---------|-------|
| `JWT_SECRET` | No | `eventmaster-shared-dev-secret-key-change-in-prod` | Must match all services |
| `USER_SERVICE_BASE_URL` | No | `http://localhost:8080` | Following list |
| `EVENT_SERVICE_BASE_URL` | No | `http://localhost:8081` | Events per creator |
| `RECOMMENDATION_SERVICE_BASE_URL` | No | `http://localhost:8082` | Ranked recommendations |

## Testing

```bash
mvn test
```
