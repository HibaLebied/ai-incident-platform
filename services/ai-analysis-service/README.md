# AI Analysis Service

This service currently builds the foundation for the future AI layer. It consumes
`incidents.created`, `incidents.updated`, and `incidents.resolved`, then stores
a local incident context in the dedicated `ai_analysis_db` database.

The service owns this projection. It never reads the incident-service database.
The local Kafka contract is deliberately defined in this service so the two
microservices remain independently deployable and can evolve their contracts
with explicit schema versions.

## Local setup

The AI service uses its dedicated `ai-analysis-postgres` container on host port
`5433`. It does not access the existing `log-postgres` databases.

Create the database in the pgvector container before starting the service:

```sql
CREATE DATABASE ai_analysis_db;
```

With the shared Docker PostgreSQL container used by this project, the same
operation can be run with:

```powershell
docker exec -it ai-analysis-postgres psql -U postgres -c "CREATE DATABASE ai_analysis_db;"
```

If the database already exists, PostgreSQL will report that fact and no data
needs to be copied from `incident_db`.

The default development host is `localhost:5433` and the default user is
`postgres`. The default local password is `postgres`; override it with
the connection with:

```text
AI_ANALYSIS_DB_URL
AI_ANALYSIS_DB_USERNAME
AI_ANALYSIS_DB_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
```

The service listens on port `8085`.

Start Kafka with the existing infrastructure Compose file, then start this
service from IntelliJ or with `./mvnw spring-boot:run`. Once an incident has
been created by `incident-service`, verify its local copy with:

```text
GET http://localhost:8085/api/incidents/{incidentId}
```

For the complete AI-oriented context, use:

```text
GET http://localhost:8085/api/incidents/{incidentId}/context
```

The context response contains the incident snapshot, its anomaly projections,
and the ordered local timeline. Incident events use schema version `2` when
they carry the optional anomaly that was just attached; status-only events keep
that field empty.

## Flow

```text
incidents.created / incidents.updated / incidents.resolved
        -> IncidentCreatedConsumer
        -> IncidentProjectionService
        -> incident_projections
        -> incident_anomaly_projections
        -> incident_timeline_events
        -> GET /api/incidents/{incidentId}/context
```

`incidentId` is unique in the database. The application checks for an existing
projection and updates it when an event is replayed, while PostgreSQL protects
the incident and anomaly invariants with unique constraints. The incident event
contains an optional anomaly payload because the original incident snapshot did
not contain enough information to rebuild anomaly history in a separate service.

## RAG foundation

The first RAG building blocks are now present, without an LLM:

```text
text or incident context -> deterministic chunks -> embeddings -> pgvector
                                                        -> cosine search
```

`KnowledgeDocumentRepository` uses JDBC for the vector column deliberately.
The incident projections remain JPA entities, while PostgreSQL's `vector` type
is isolated behind native SQL. This avoids coupling the whole projection model
to a Hibernate-specific vector mapping.

The default provider is `MOCK` with dimension `8`. It is deterministic and
useful for local tests only; it is not a production semantic model. The provider
and dimension are configuration properties so a real embedding implementation
can replace it later without changing retrieval or storage contracts.

Before starting the service, the dedicated PostgreSQL container must provide
pgvector and the database must exist:

```sql
CREATE DATABASE ai_analysis_db;
\c ai_analysis_db
CREATE EXTENSION IF NOT EXISTS vector;
```

The service creates its `knowledge_documents` table and the unique
`(document_id, chunk_index)` constraint on startup. The current setup uses
PostgreSQL 16.15 with pgvector 0.8.6.

Development endpoints:

```text
POST /api/rag/documents
POST /api/rag/documents/file
POST /api/rag/historical-incidents/{incidentId}
POST /api/rag/search
```

For a real Markdown or text file:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8085/api/rag/documents/file?documentId=db-runbook-file&title=Database%20Runbook&source=local-documents&documentType=RUNBOOK" `
  -Method Post `
  -Form @{ file = Get-Item .\knowledge\database.md }
```

Example search request:

```json
{"query":"database connection pool exhausted","topK":5}
```

Historical incidents are indexed explicitly through the second endpoint. The
service does not automatically index every incident, which keeps indexing a
deliberate operation and prevents uncontrolled growth of the knowledge base.

If `ai_analysis_db` already contains projections created during Lot 12, the new
tables are added by `ddl-auto=update`, but old events cannot retroactively gain
anomaly details. For a clean development replay, recreate the AI database or
reset the `ai-analysis-group` offsets while the incident topics are retained.
