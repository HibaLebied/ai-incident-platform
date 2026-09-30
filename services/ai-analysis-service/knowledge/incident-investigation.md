# Incident Investigation Method

## Establish the timeline

Start with the first observed anomaly and the most recent occurrence. Record
the event time, detection time, service, environment, trace ID, fingerprint,
and affected operation. The first anomaly is not automatically the root cause;
it is the beginning of the available evidence.

## Separate evidence from inference

Evidence is directly observed, such as an HTTP 500 response, a database
timeout, a Kafka lag increase, or a deployment event. An inference is a
possible explanation derived from evidence. Recommendations are actions that
may reduce impact or collect more information. The analysis must keep these
categories separate.

## Correlate anomalies

Group anomalies that share a service, environment, and problem fingerprint.
Compare their timestamps and traces to understand whether they represent one
operational problem or several independent problems. A trace ID is useful for
following one request, while a fingerprint is more useful for recognizing a
recurrent failure pattern.

## Compare historical incidents

Search previous incidents using service, environment, anomaly description,
error messages, affected dependencies, and remediation notes. Do not rely only
on an exact fingerprint because a similar failure can have a different message
or a different generated identifier.

Historical incidents are useful evidence, but similarity does not prove that
the current incident has the same root cause. Confirm the hypothesis against
current logs, metrics, traces, and deployment information.

## Common investigation questions

1. What changed immediately before the anomaly appeared?
2. Which services and environments are affected?
3. Is the problem reproducible or intermittent?
4. Is there a dependency timeout or resource limit?
5. Did the error rate change after a deployment?
6. What evidence confirms or contradicts the leading hypothesis?
7. What action reduces impact without destroying evidence?

## Closure

Resolve an incident only after monitoring confirms recovery. Record the cause,
the evidence used, the remediation, the remaining uncertainty, and follow-up
actions. A good historical record should help another engineer investigate a
similar problem without treating an old hypothesis as a guaranteed fact.
