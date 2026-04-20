---
status: awaiting_human_verify
trigger: "csdn-sync-empty-content: CSDN sync runs but all synced articles have empty content and no title"
created: 2026-04-16T00:00:00.000Z
updated: 2026-04-16T00:00:00.000Z
---

## Current Focus
hypothesis: "CSDN JSON API is returning a CAPTCHA page instead of JSON because required headers (Accept-Language, Origin) are missing from the request. The CAPTCHA HTML contains URL-like strings that match the regex, so invalid article URLs are extracted. When fetchArticleHtml tries to fetch these URLs, it gets error/CAPTCHA pages with no content."
test: "Compare curl with and without Accept-Language and Origin headers"
expecting: "With proper headers, API returns JSON. Without them, API returns CAPTCHA."
next_action: "Fix the missing headers in CsdnArticleFetcherImpl.java fetchArticleList()"

## Symptoms
expected: Articles synced from CSDN should have title, content (markdown), tags populated from CSDN article pages
actual: Articles are created in DB but title="Untitled" and content="" — effectively empty articles
errors: No explicit errors; sync completes with counts but articles have no useful content
reproduction: Run sync via admin UI or API — articles get created but are empty
started: Started after phase 05-06 (JSON API switch). HTML scraping approach (05-06 replaced it) was previously broken.

## Evidence
- timestamp: 2026-04-16T00:00:00Z
  checked: "CSDN JSON API without Accept-Language/Origin headers"
  found: "API returns HTML security verification page (CAPTCHA wall). Title: '请进行安全验证(Security Verification)'. Contains no JSON."
  implication: "Without proper headers, CSDN returns CAPTCHA instead of JSON."
- timestamp: 2026-04-16T00:00:00Z
  checked: "CSDN JSON API WITH Accept-Language and Origin headers"
  found: "API returns valid JSON: {\"code\":200,\"data\":{\"list\":[{\"articleId\":159995529,\"title\":\"java springboot3 后端 基础框架\",\"url\":\"https://blog.csdn.net/2301_78723800/article/details/159995529\",...}],\"total\":45}}"
  implication: "Adding Accept-Language: zh-CN,zh;q=0.9,en;q=0.8 and Origin: https://blog.csdn.net makes CSDN return JSON."
- timestamp: 2026-04-16T00:00:00Z
  checked: "CSDN JSON API without noHtml=1 parameter"
  found: "API returns CAPTCHA HTML even with all headers."
  implication: "The noHtml=1 parameter is required to get JSON response."
- timestamp: 2026-04-16T00:00:00Z
  checked: "Current code headers in CsdnArticleFetcherImpl.java"
  found: "Only sets User-Agent, Accept, Referer. Missing Accept-Language and Origin headers."
  implication: "Backend's requests to CSDN JSON API are likely returning CAPTCHA pages instead of JSON."
- timestamp: 2026-04-16T00:00:00Z
  checked: "CSDN article HTML with curl"
  found: "Article HTML contains h1.title-article and div.article_content selectors. HTML response is 236KB."
  implication: "Selectors in parseArticle are correct for actual CSDN HTML."

## Eliminated
<!-- APPEND only -->
- hypothesis: "JSON API returns relative URLs instead of absolute, causing regex to fail"
  evidence: "Curl test shows API returns full HTTPS URLs like https://blog.csdn.net/2301_78723800/article/details/159995529"
  timestamp: 2026-04-16T00:00:00Z
- hypothesis: "CSS selectors (h1.title-article, div.article_content) are wrong for current CSDN HTML"
  evidence: "Curl test shows h1.title-article and div.article_content ARE present in the HTML"
  timestamp: 2026-04-16T00:00:00Z
- hypothesis: "JSON API itself is broken and returns no URLs"
  evidence: "JSON API returns valid article URLs when called with proper headers"
  timestamp: 2026-04-16T00:00:00Z
- hypothesis: "noHtml=1 parameter is the issue"
  evidence: "noHtml=1 is already in the code and is required to get JSON. Without it, API returns HTML (CAPTCHA)."
  timestamp: 2026-04-16T00:00:00Z

## Eliminated
<!-- APPEND only -->

## Resolution
root_cause: "CsdnArticleFetcherImpl was missing Accept-Language and Origin headers in HTTP requests to CSDN. Without these headers, CSDN's anti-bot protection returns a CAPTCHA page instead of JSON for the article list API, and may also return non-content HTML for article page fetches. The regex for extracting URLs would fail on CAPTCHA HTML, but the article HTML fetch might get CAPTCHA/error pages with no content, causing parseArticle to return 'Untitled' and empty content."
fix: "Added Accept-Language: zh-CN,zh;q=0.9,en;q=0.8 and Origin: https://blog.csdn.net headers to both fetchArticleList() (JSON API) and fetchArticleHtml() (article pages) methods."
verification: "Backend compiles. Verified with curl: JSON API returns proper article list with new headers; article HTML contains expected selectors (h1.title-article, div.article_content). Need manual verification: restart backend and run actual sync."
files_changed: ["backend/src/main/java/com/blog/service/impl/CsdnArticleFetcherImpl.java"]
