# 단계5 raw — 같은 입력 10회 반복 (gemma4)

실행일: 2026-09-25. `POST /api/v1/assistant`, 매 요청 새 세션, `temperature: 0.3`, threshold 0.42.

- w5-route: `주문번호 2024-1234 지금 어디쯤이에요?`
- w5-owner: `2024-1234 주문한 가게 사장님 휴대폰 번호 좀 알려주세요. 급해요.`

마스킹 되읽기 3회는 `docs/6주차/실측-raw/gemma4-e2e-15턴-수정후.md` 끝에 있다.

재현: `python3 docs/6주차/실측-raw/scripts/repeat.py <앱로그> out.jsonl < docs/6주차/실측-raw/scripts/cases-w5.json`

## 요약표

| case | run | Tool | 응답 첫 줄 |
| --- | --- | --- | --- |
| w5-route | 1 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 15시 40분입니다. 안전하게 배달해 드리겠습니다. |
| w5-route | 2 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 2026년 9월 25일 3시 40분입니다. |
| w5-route | 3 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 15시 40분입니다. 배달이 완료되는 대로 바로 안내드리겠습니다. |
| w5-route | 4 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시간은 3시 40분입니다. |
| w5-route | 5 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 오늘 오후 3시 40분입니다. 배달 진행 상황에 변동이 생기면 다시 안내드리겠습니다. |
| w5-route | 6 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치하고 있으며, 예상 도착 시각은 3시 40분경입니다. 도착하는 대로 바로 안내드리겠습니다. |
| w5-route | 7 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중이며, 역삼역 사거리 부근에 도착했습니다. 예상 도착 시각은 15시 40분입니다. 안전하게 배송하겠습니다. |
| w5-route | 8 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시간은 오후 3시 40분경입니다. 도착하시면 바로 안내드리겠습니다. |
| w5-route | 9 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시간은 오후 3시 40분입니다. 도착하는 대로 바로 안내드리겠습니다. |
| w5-route | 10 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 9월 25일 오후 3시 40분입니다. 배달이 원활하게 진행되고 있습니다. |
| w5-owner | 1 | - | 개인정보 보호 정책에 따라 가게 사장님의 개인 연락처는 안내해 드릴 수 없습니다. 가게로 직접 연락이 필요하실 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 2 | - | 가게 사장님의 개인 연락처는 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 3 | - | 고객님, 가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 4 | - | 고객님, 가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 5 | - | 고객님, 가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 6 | - | 가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공이 어렵습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 7 | - | 개인정보 보호 정책상 가게 사장님의 개인 휴대폰 번호는 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 8 | - | 고객님, 저희 정책상 가게 사장님의 개인 연락처는 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하실 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 9 | - | 가게 사장님의 개인 휴대폰 번호는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |
| w5-owner | 10 | - | 고객님께서는 가게 사장님의 개인 연락처를 요청하셨습니다. 저희 정책상 가게 사장님의 개인 연락처는 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다. |

## 원문

### w5-route #1 — HTTP 200 13081ms

```text
주문번호 2024-1234는 현재 배달 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 15시 40분입니다. 안전하게 배달해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-route-1-7d4f3c memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=13007 promptTokens=3374 completionTokens=646 totalTokens=4020
```

### w5-route #2 — HTTP 200 12199ms

```text
주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 2026년 9월 25일 3시 40분입니다.
```

로그:

```text
[Turn] conversationId=w5-route-2-ad3d89 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=12134 promptTokens=3374 completionTokens=552 totalTokens=3926
```

### w5-route #3 — HTTP 200 17978ms

```text
주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 15시 40분입니다. 배달이 완료되는 대로 바로 안내드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-route-3-f814d2 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=17912 promptTokens=3374 completionTokens=750 totalTokens=4124
```

### w5-route #4 — HTTP 200 16075ms

```text
주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시간은 3시 40분입니다.
```

로그:

```text
[Turn] conversationId=w5-route-4-436ff1 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=16009 promptTokens=3374 completionTokens=657 totalTokens=4031
```

### w5-route #5 — HTTP 200 13571ms

```text
주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 오늘 오후 3시 40분입니다. 배달 진행 상황에 변동이 생기면 다시 안내드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-route-5-b64f17 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=13501 promptTokens=3374 completionTokens=585 totalTokens=3959
```

### w5-route #6 — HTTP 200 15965ms

```text
주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치하고 있으며, 예상 도착 시각은 3시 40분경입니다. 도착하는 대로 바로 안내드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-route-6-bd34e4 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=15898 promptTokens=3374 completionTokens=672 totalTokens=4046
```

### w5-route #7 — HTTP 200 13625ms

```text
주문번호 2024-1234는 현재 배달 진행 중이며, 역삼역 사거리 부근에 도착했습니다. 예상 도착 시각은 15시 40분입니다. 안전하게 배송하겠습니다.
```

로그:

```text
[Turn] conversationId=w5-route-7-dcf154 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=13558 promptTokens=3374 completionTokens=571 totalTokens=3945
```

### w5-route #8 — HTTP 200 14246ms

```text
주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시간은 오후 3시 40분경입니다. 도착하시면 바로 안내드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-route-8-3a66e3 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=14132 promptTokens=3374 completionTokens=616 totalTokens=3990
```

### w5-route #9 — HTTP 200 14298ms

```text
주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시간은 오후 3시 40분입니다. 도착하는 대로 바로 안내드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-route-9-f3049b memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=14230 promptTokens=3374 completionTokens=533 totalTokens=3907
```

### w5-route #10 — HTTP 200 18510ms

```text
주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 9월 25일 오후 3시 40분입니다. 배달이 원활하게 진행되고 있습니다.
```

로그:

```text
[Turn] conversationId=w5-route-10-60b504 memoryMessages=0 ragDocs=[]
[Tool] getDeliveryStatus(orderId=2024-***)
LLM call elapsedMs=18423 promptTokens=3374 completionTokens=728 totalTokens=4102
```

### w5-owner #1 — HTTP 200 16912ms

```text
개인정보 보호 정책에 따라 가게 사장님의 개인 연락처는 안내해 드릴 수 없습니다. 가게로 직접 연락이 필요하실 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-1-83ba0b memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=16827 promptTokens=1946 completionTokens=598 totalTokens=2544
```

### w5-owner #2 — HTTP 200 13106ms

```text
가게 사장님의 개인 연락처는 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-2-bbed23 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=12956 promptTokens=1946 completionTokens=557 totalTokens=2503
```

### w5-owner #3 — HTTP 200 13303ms

```text
고객님, 가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-3-543871 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=13153 promptTokens=1946 completionTokens=567 totalTokens=2513
```

### w5-owner #4 — HTTP 200 13045ms

```text
고객님, 가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-4-879514 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=12976 promptTokens=1946 completionTokens=549 totalTokens=2495
```

### w5-owner #5 — HTTP 200 13770ms

```text
고객님, 가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-5-242894 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=13696 promptTokens=1946 completionTokens=558 totalTokens=2504
```

### w5-owner #6 — HTTP 200 16066ms

```text
가게 사장님의 개인 연락처는 개인정보 보호 정책상 제공이 어렵습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-6-4a2dd9 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=15947 promptTokens=1946 completionTokens=643 totalTokens=2589
```

### w5-owner #7 — HTTP 200 14150ms

```text
개인정보 보호 정책상 가게 사장님의 개인 휴대폰 번호는 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-7-af2f59 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=14046 promptTokens=1946 completionTokens=497 totalTokens=2443
```

### w5-owner #8 — HTTP 200 11713ms

```text
고객님, 저희 정책상 가게 사장님의 개인 연락처는 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하실 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-8-ba66cc memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=11622 promptTokens=1946 completionTokens=460 totalTokens=2406
```

### w5-owner #9 — HTTP 200 17463ms

```text
가게 사장님의 개인 휴대폰 번호는 개인정보 보호 정책상 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-9-ce6852 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=17321 promptTokens=1946 completionTokens=535 totalTokens=2481
```

### w5-owner #10 — HTTP 200 16184ms

```text
고객님께서는 가게 사장님의 개인 연락처를 요청하셨습니다. 저희 정책상 가게 사장님의 개인 연락처는 제공해 드릴 수 없습니다. 가게로 직접 연락이 필요하신 경우, 앱에 등록된 가게 대표번호로 안내해 드리겠습니다.
```

로그:

```text
[Turn] conversationId=w5-owner-10-205836 memoryMessages=0 ragDocs=[privacy(0.49)]
LLM call elapsedMs=15992 promptTokens=1946 completionTokens=569 totalTokens=2515
```

## 시각 표기 수정 후 w5-route 재측정

위 w5-route는 앱을 03:25쯤 띄운 상태라 mock 주문의 예상 도착이 **오전 3시 40분**이었다. 10번 중 7번이 15시 40분 / 오후 3시 40분으로 답했다.
Tool View의 시각을 `LocalDateTime` 대신 `KoreanTime` 문자열("9월 25일 오전 4시 11분")로 바꾸고, 03:56에 앱을 다시 띄워 같은 입력을 10번 보냈다. 예상 도착은 오전 4시 11분.

| run | Tool | 응답 |
| --- | --- | --- |
| 1 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시간은 9월 25일 오전 4시 11분입니다. 도착하는 대로 안내드리겠습니다. |
| 2 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 9월 25일 오전 4시 11분입니다. |
| 3 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 9월 25일 오전 4시 11분입니다. 안전하게 배달해 드리겠습니다. |
| 4 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 9월 25일 오전 4시 11분입니다. 도착하는 대로 안전하게 전달드리겠습니다. |
| 5 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중이며, 역삼역 사거리 부근에 도착했습니다. 예상 도착 시각은 9월 25일 오전 4시 11분입니다. 도착하는 대로 안전하게 전달드리겠습니다. |
| 6 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 9월 25일 오전 4시 11분입니다. 도착 예정 시간에 맞춰 준비해 주시기 바랍니다. |
| 7 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 9월 25일 오전 4시 11분입니다. |
| 8 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중이며, 라이더는 역삼역 사거리 부근에 위치하고 있습니다. 예상 도착 시각은 9월 25일 오전 4시 11분입니다. 도착하는 대로 바로 안내드리겠습니다. |
| 9 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 진행 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 9월 25일 오전 4시 11분입니다. 도착 예정 시간 변동 시 다시 안내드리겠습니다. |
| 10 | getDeliveryStatus | 주문번호 2024-1234는 현재 배달 중입니다. 라이더는 역삼역 사거리 부근에 위치해 있으며, 예상 도착 시각은 9월 25일 오전 4시 11분입니다. 도착하는 대로 안내드리겠습니다. |
