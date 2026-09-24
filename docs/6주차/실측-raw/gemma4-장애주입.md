# 장애 주입 raw — PgVector DOWN / Ollama DOWN

실행일: 2026-09-25. 환경은 E2E raw와 같다.

## 수정 전 — PgVector DOWN

코드 상태: E2E 수정 후와 같음 (FailSoftVectorStore 없음, Hikari connection-timeout 기본 30s).

재현:

```bash
docker stop baedal-pgvector
curl -s localhost:18080/actuator/health
curl -s -X POST localhost:18080/api/v1/assistant -H 'Content-Type: application/json' -H 'X-Session-Id: pg-down-1' -d '{"message":"비 오는 날 늦게 오면 보상 받나요?"}'
curl -s -X POST localhost:18080/api/v1/assistant -H 'Content-Type: application/json' -H 'X-Session-Id: pg-down-2' -d '{"message":"주문번호 2024-1234 지금 어디쯤이에요?"}'
docker start baedal-pgvector
```

```text
## docker stop baedal-pgvector
baedal-pgvector
## health
DOWN {'db': 'DOWN', 'diskSpace': 'UP', 'ollama': 'UP', 'ping': 'UP', 'ssl': 'UP'}
$ POST /api/v1/assistant {"message":"비 오는 날 늦게 오면 보상 받나요?"}
죄송합니다. 일시적인 오류로 요청을 처리하지 못했습니다. 잠시 후 다시 시도하시거나 고객센터 1600-0987로 문의해 주세요.
HTTP 200 30.090112s

$ POST /api/v1/assistant {"message":"주문번호 2024-1234 지금 어디쯤이에요?"}
죄송합니다. 일시적인 오류로 요청을 처리하지 못했습니다. 잠시 후 다시 시도하시거나 고객센터 1600-0987로 문의해 주세요.
HTTP 200 30.073734s

## docker start baedal-pgvector
baedal-pgvector
## health after start
UP {'db': 'UP', 'diskSpace': 'UP', 'ollama': 'UP', 'ping': 'UP', 'ssl': 'UP'}
$ POST /api/v1/assistant {"message":"비 오는 날 늦게 오면 보상 받나요?"}
비가 온다는 사실만으로는 보상 대상이 아닙니다. 보상은 기상 특보 발효 여부와 실제 지연 시간을 기준으로 합니다.

일반 강수 상황에서도 배달이 예상 시간보다 지연된 경우, 지연 시간에 따라 보상 기준이 적용됩니다.

*   **예상 시간 + 11~29분** 지연 시: 다음 주문에 사용 가능한 1,000원 쿠폰이 자동 지급됩니다.
*   **예상 시간 + 30~59분** 지연 시: 배달비 전액 환불 또는 3,000원 쿠폰 중 선택 가능합니다.
*   **예상 시간 + 60분 이상** 지연 시: 전액 환불 검토 대상이므로 상담원 연결이 필요합니다.

다만, 태풍, 폭우 등 기상 특보가 발효된 경우의 지연은 일반 지연 보상 기준에서 제외됩니다.
HTTP 200 16.985226s

## app log
(HikariPool-1 - Failed to validate connection ... 10줄 생략)
03:42:38.645 WARN  DataSourceHealthIndicator : DataSource health check failed
ramework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30005ms (total=0, active=0, idle=0, waiting=0)
org.postgresql.util.PSQLException: Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
java.net.ConnectException: Connection refused
03:42:38.652 WARN  HealthEndpointSupport : Health contributor org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator (db) took 30016ms to respond
03:43:08.755 ERROR AssistantController : [Fallback] assistant 처리 실패 — 내부 오류 (응답에는 미노출)
ramework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30004ms (total=0, active=0, idle=0, waiting=0)
org.postgresql.util.PSQLException: Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
java.net.ConnectException: Connection refused
03:43:38.842 ERROR AssistantController : [Fallback] assistant 처리 실패 — 내부 오류 (응답에는 미노출)
ramework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30004ms (total=0, active=0, idle=0, waiting=0)
org.postgresql.util.PSQLException: Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
java.net.ConnectException: Connection refused
03:43:45.293 INFO  TurnTraceAdvisor : [Turn] conversationId=pg-up-1 memoryMessages=0 ragDocs=[delay-compensation(0.46), weather-delay(0.43)]
03:44:02.168 INFO  PerformanceLoggingAdvisor : LLM call elapsedMs=16874 promptTokens=2393 completionTokens=756 totalTokens=3149
```

## 수정 후 — PgVector DOWN / Ollama DOWN

코드 상태: `FailSoftVectorStore` + `spring.datasource.hikari.connection-timeout: 2000` 적용.

Ollama DOWN은 로컬 Ollama 프로세스를 죽이지 않았다. 같은 머신의 다른 작업이 쓰는 서버라서다.
대신 앱을 `--spring.ai.ollama.base-url=http://127.0.0.1:11435`로 띄우고, 11435→11434 TCP 포워더(`scripts/proxy.py`)를 끊었다가 다시 띄웠다.
앱 입장에서는 채팅·임베딩·헬스체크 모두 Connection refused가 난다. 응답이 느려지는 장애(타임아웃)는 이 방식으로 재현하지 않았다.

```text
# A. PgVector DOWN (fail-soft + hikari 2s 적용 후)
## health (pgvector stopped)
DOWN {'db': 'DOWN', 'diskSpace': 'UP', 'ollama': 'UP', 'ping': 'UP', 'ssl': 'UP'} [2.011366s]
$ POST /api/v1/assistant {"message":"비 오는 날 늦게 오면 보상 받나요?"}
해당 내용은 확인이 필요합니다. 상담원 연결로 도와드리겠습니다.
HTTP 200 12.491406s

$ POST /api/v1/assistant {"message":"주문번호 2024-1234 지금 어디쯤이에요?"}
주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 있습니다. 예상 도착 시각은 오늘 오전 4시 43분입니다. 배송 상황을 실시간으로 확인해 드리겠습니다.
HTTP 200 15.152301s

## app log
03:46:01.804 WARN  DataSourceHealthIndicator : DataSource health check failed
ramework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 2002ms (total=0, active=0, idle=0, waiting=0)
org.postgresql.util.PSQLException: Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
java.net.ConnectException: Connection refused
03:46:03.906 WARN  FailSoftVectorStore : [RAG] 검색 실패 — Context 없이 진행 type=CannotGetJdbcConnectionException message=Failed to obtain JDBC Connection
03:46:03.930 INFO  TurnTraceAdvisor : [Turn] conversationId=v3-pg-down-1 memoryMessages=0 ragDocs=[]
03:46:14.304 INFO  PerformanceLoggingAdvisor : LLM call elapsedMs=10373 promptTokens=1632 completionTokens=459 totalTokens=2091
03:46:16.393 WARN  FailSoftVectorStore : [RAG] 검색 실패 — Context 없이 진행 type=CannotGetJdbcConnectionException message=Failed to obtain JDBC Connection
03:46:16.394 INFO  TurnTraceAdvisor : [Turn] conversationId=v3-pg-down-2 memoryMessages=0 ragDocs=[]
03:46:20.422 INFO  OrderTools : [Tool] getDeliveryStatus(orderId=2024-***)
03:46:29.474 INFO  PerformanceLoggingAdvisor : LLM call elapsedMs=13080 promptTokens=3371 completionTokens=555 totalTokens=3926
## health (pgvector started)
UP {'db': 'UP', 'diskSpace': 'UP', 'ollama': 'UP', 'ping': 'UP', 'ssl': 'UP'} [0.032290s]
# B. Ollama DOWN (앱→Ollama 프록시 종료)
## health (proxy killed)
DOWN {'db': 'UP', 'diskSpace': 'UP', 'ollama': 'DOWN', 'ping': 'UP', 'ssl': 'UP'} [0.008945s]
$ POST /api/v1/assistant {"message":"주문번호 2024-1234 지금 어디쯤이에요?"}
죄송합니다. 일시적인 오류로 요청을 처리하지 못했습니다. 잠시 후 다시 시도하시거나 고객센터 1600-0987로 문의해 주세요.
HTTP 200 0.009736s

$ POST /api/v1/assistant {"message":"상담원 바꿔주세요"}
상담원 연결을 도와드리겠습니다. 연결이 지연되면 고객센터 1600-0987로 전화 주세요.
HTTP 200 0.002134s

## app log
03:47:20.371 WARN  FailSoftVectorStore : [RAG] 검색 실패 — Context 없이 진행 type=ResourceAccessException message=I/O error on POST request for "http://127.0.0.1:11435/api/embed": Connection refused: /127.0.0.1:11435
03:47:20.372 INFO  TurnTraceAdvisor : [Turn] conversationId=v3-ollama-down-1 memoryMessages=0 ragDocs=[]
03:47:20.374 WARN  PerformanceLoggingAdvisor : LLM call failed elapsedMs=1 type=ResourceAccessException message=I/O error on POST request for "http://127.0.0.1:11435/api/chat": Connection refused: /127.0.0.1:11435
03:47:20.374 ERROR AssistantController : [Fallback] assistant 처리 실패 — 내부 오류 (응답에는 미노출)
ramework.web.client.ResourceAccessException: I/O error on POST request for "http://127.0.0.1:11435/api/chat": Connection refused: /127.0.0.1:11435
io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: /127.0.0.1:11435
java.net.ConnectException: Connection refused
## health (proxy restored)
UP {'db': 'UP', 'diskSpace': 'UP', 'ollama': 'UP', 'ping': 'UP', 'ssl': 'UP'} [0.008959s]
$ POST /api/v1/assistant {"message":"주문번호 2024-1234 지금 어디쯤이에요?"}
주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 9월 25일 목요일 오전 4시 43분입니다. 도착하는 대로 안전하게 전달드리겠습니다.
HTTP 200 12.872532s

## metrics
{"name":"baedal.agent.fallback","description":"Assistant requests answered by safe fallback","measurements":[{"statistic":"COUNT","value":1.0}],"availableTags":[]}
{"name":"baedal.agent.rag.failure","description":"Vector searches that failed and continued without context","measurements":[{"statistic":"COUNT","value":3.0}],"availableTags":[]}
baedal_agent_rag_failure_total 3.0
baedal_agent_tokens_total{type="completion"} 1662.0
baedal_agent_tokens_total{type="prompt"} 8374.0
```
