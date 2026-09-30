# Kafka Retry and Dead Letter Guide

## Purpose

This guide explains how to investigate a Kafka consumer that repeatedly fails
to process messages. The goal is to distinguish a temporary infrastructure
problem from a permanent data or contract problem.

## First checks

Confirm that the Kafka broker is reachable and that the consumer group is
running. Check the consumer group lag, the assigned partitions, and the time
of the oldest unprocessed message. A growing lag means that production is
faster than consumption or that a consumer is blocked.

Inspect the application logs for deserialization errors, validation errors,
database timeouts, and transaction rollbacks. A deserialization error usually
means that the message contract is incompatible with the consumer DTO or that
the JSON payload is malformed.

## Retry behavior

Transient failures such as a short database outage should be retried with a
bounded backoff. The consumer must not retry forever because one bad message
could block an entire partition. The retry policy should be visible in the
application configuration and should include the maximum number of attempts.

After the retry limit is reached, publish the message to a dead letter topic.
The dead letter record should preserve the original payload, the original
topic, partition, offset, exception type, and failure timestamp.

## Dead letter investigation

Before replaying a dead letter message, identify the root cause. Correct the
schema, configuration, or dependency problem first. Replaying without fixing
the cause can create another retry storm.

When replaying a message, use an explicit tool or administrative command and
record who performed the replay. Validate that the consumer is idempotent so
that a replay cannot create duplicate database records.

## Operational checklist

1. Check broker health and network connectivity.
2. Check consumer group lag and partition ownership.
3. Inspect the exception and the original event payload.
4. Decide whether the failure is transient or permanent.
5. Verify database availability and transaction rollback behavior.
6. Repair the cause before replaying the dead letter message.
