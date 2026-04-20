# Debug Session: CSDN Sync Returns All Zeros

**Phase:** 05-image-handling-sync-ui
**Truth (expected behavior):** After sync completes, an inline results panel appears showing color-coded counts for each result type: green text for success count, yellow/amber for update count, gray for skip count, and red for error count. The counts reflect the actual outcome of the sync operation.

**Symptom:** Sync returns 200 but all counts are zero: `{"created":0,"updated":0,"skipped":0,"errors":[]}`

**Root Cause Found:** `CsdnArticleFetcherImpl` uses `@Autowired RestClient.Builder restClientBuilder` to make HTTP calls, but **no `RestClient.Builder` bean is defined anywhere in the codebase**. Spring Boot 3.x does NOT auto-create a `RestClient.Builder` bean by default — it must be explicitly declared via `@Bean`. As a result, `restClientBuilder` is `null`, causing a NullPointerException on every `fetchHtml()` call. All article fetches fail silently (caught by the continue-on-error handler), the article list is empty, and the sync returns all zeros.

## Evidence

- `CsdnArticleFetcherImpl.java` line 26: `private RestClient.Builder restClientBuilder;` — injected bean is null because no such bean exists
- `CsdnArticleFetcherImpl.java` lines 63-68: `fetchHtml()` calls `restClientBuilder.build().get().uri(url).retrieve().body(String.class)` — NPE when `null.build()` is called
- `CsdnSyncServiceImplImpl.java` lines 72-125: article loop catches all exceptions and continues, so fetch failures result in zero counts
- No `RestClient.Builder` `@Bean` exists in `SecurityConfig`, `WebConfig`, or any other `@Configuration` class
- `CsdnSyncServiceImpl.java` line 55: on error path returns `new SyncResultResponse(0, 0, 0, List.of("..."))` — the silent error means we get `{created:0, updated:0, skipped:0, errors:[]}` because all URLs failed in the loop

## Files Involved

- `backend/src/main/java/com/blog/service/impl/CsdnArticleFetcherImpl.java` — `restClientBuilder` field is null
- `backend/src/main/java/com/blog/config/WebConfig.java` — missing `RestClient.Builder` bean definition
- `backend/src/main/java/com/blog/config/SecurityConfig.java` — no `RestClient.Builder` bean here either

## Suggested Fix Direction

Add a `RestClient.Builder` bean to Spring configuration. Simplest fix: add `RestClient.Builder` as a `@Bean` method in `WebConfig` (or a new `RestClientConfig` class). Alternatively, change `CsdnArticleFetcherImpl` to use `RestClient.create()` instead of injecting `RestClient.Builder` (Spring Boot auto-configures a default RestClient when using the static factory method).

## Investigation Completed

2026-04-16
