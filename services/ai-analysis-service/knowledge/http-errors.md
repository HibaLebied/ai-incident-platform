# HTTP Server Error Investigation

## HTTP 500

An HTTP 500 response means that the server failed while processing the
request. It is an application symptom, not a root cause. The investigation
must use the timestamp, service name, trace identifier, request route, and
application logs to locate the failing component.

Start by checking whether the error affects all requests or only one route.
Compare the error rate with the deployment timeline and recent configuration
changes. A sudden increase immediately after a deployment suggests a
regression, incompatible configuration, or missing dependency.

## Database failures

If the error message mentions a connection pool, inspect active connections,
pool exhaustion, database availability, and slow queries. A pool can be
exhausted when connections are leaked, transactions are too long, or the
database is overloaded.

Do not increase the pool size immediately. A larger pool can increase database
contention and make the outage worse. First determine whether connections are
blocked, whether queries are slow, and whether the database has reached its
connection limit.

## Dependency failures

For an upstream timeout, check the dependency latency, timeout configuration,
retry policy, and circuit breaker state. Retries should be bounded and should
use backoff. Retrying a failing dependency for every request can amplify the
incident.

## Evidence to collect

Collect representative request IDs, trace IDs, error messages, stack traces,
latency percentiles, HTTP status counts, database metrics, and deployment
versions. Store evidence with the incident timeline so that later analysis can
distinguish observed facts from hypotheses.

## Recovery

If a bad deployment caused the error, follow the rollback procedure. If the
database is overloaded, reduce request pressure and investigate expensive
queries. After recovery, verify that the error rate, latency, and dependency
health return to normal before resolving the incident.
