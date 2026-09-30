# Database Connection Pool Runbook

When the database connection pool is exhausted, inspect active connections,
slow queries, and database availability before restarting anything.

Collect the incident timestamp, service name, error messages, and connection
pool metrics. Restart the pool only after collecting diagnostic information.

## Connection pool symptoms

Typical symptoms include an increase in request latency, database timeout
exceptions, HTTP 500 responses, and a growing number of threads waiting for a
connection. Check whether the application is holding connections while it
waits for an external service. Long transactions can make a healthy database
appear unavailable to the application.

Compare the configured maximum pool size with the database connection limit.
The values must be compatible with the number of application instances and
with connections used by administration tools. Increasing the pool size can
make the database run out of connections and can increase lock contention.

## Investigation commands

Capture the number of active sessions, idle sessions, waiting sessions, and
long-running queries. Record the database name, application service, process
identifier, and incident timestamp. Do not terminate sessions until the owning
team understands the effect of the action.

Review connection acquisition time separately from query execution time. A
high acquisition time indicates pool pressure, while a high execution time
usually indicates slow queries, locks, missing indexes, or database resource
pressure.

## Recovery procedure

Reduce incoming traffic if the service is amplifying the database failure.
Check whether a recent deployment introduced a connection leak or removed a
transaction boundary. If a rollback is required, preserve the logs and
metrics first so that the incident can be investigated later.

After recovery, monitor pool usage, query latency, error rate, and database
connections for several minutes. Document the evidence, the cause, the
recovery action, and preventive work such as query optimization or a pool
timeout adjustment.
