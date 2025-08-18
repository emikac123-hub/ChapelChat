# Slow Response Row Records
## Why keep it

- Quick triage for bad UX spikes (a few 10–20s replies feel “broken”).
- Validates reliability beyond averages/p95 (keeps SLO honest).
- Surfaces patterns you can fix (content, model, time-of-day).

## What patterns to watch

- Repeated question themes (e.g., sacramental requirements → huge context).
- Specific models dragging (cheaper tier underperforms).
- High token counts (big prompts/context).
- Time windows (traffic spikes, rate limits).

## What to do when you spot slowness

- Tighten/canonize answers in church_profile (short, precise, cacheable).
- Switch/tune model/region if one vendor path degrades.
- Adjust retrieval (chunking, timeouts) if RAG is the bottleneck.

## Role visibility

- Show only to OWNER/ADMIN/ANALYST.
- Others see a simple “Latency healthy” badge.

## Columns to include

- timestamp, model, latency_ms, total_tokens, source (web/admin), truncated user_question, link to conversation.

## Filters to add

- Quick ranges: last 24h / 7d.
- By model.
- flagged-only.

## Handy actions

- Open conversation.
- Create canned answer (for recurring slow Qs).
- Mark as “known slow” (expected).
- Soft alerts
- Notify if any response > N seconds in last hour.
- Notify if count of > N sec exceeds baseline (e.g., > 2× p95).