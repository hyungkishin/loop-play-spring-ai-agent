# RAG threshold 점수 분포 raw

질문을 `qwen3-embedding:0.6b`로 임베딩해 `vector_store` 7개 청크와 코사인 유사도(`1 - (embedding <=> q)`)를 직접 계산했다. Spring AI `QuestionAnswerAdvisor`가 쓰는 것과 같은 테이블, 같은 임베딩 모델이다.

재현: `python3 scripts/scores.py "질문1" "질문2" ...`

```text
Q: 비 오는 날 늦게 오면 보상 받나요?
  delay-compensation 0.456 | weather-delay 0.426 | refund-after-delivered 0.269 | refund-basic 0.255 | coupon-faq 0.232 | cancel-policy 0.198 | privacy 0.159
Q: 비 오는 날 지연이면 보상 받나요?
  delay-compensation 0.565 | weather-delay 0.511 | refund-basic 0.313 | refund-after-delivered 0.313 | coupon-faq 0.305 | cancel-policy 0.239 | privacy 0.174
Q: 음식이 너무 늦게 왔어요 보상 되나요
  delay-compensation 0.526 | weather-delay 0.433 | refund-after-delivered 0.388 | coupon-faq 0.387 | refund-basic 0.382 | cancel-policy 0.314 | privacy 0.168
Q: 라이더가 한참 안 와요. 뭐 해주는 거 있어요?
  weather-delay 0.307 | delay-compensation 0.273 | privacy 0.219 | coupon-faq 0.179 | refund-basic 0.164 | refund-after-delivered 0.159 | cancel-policy 0.111
Q: 쿠폰 적용 안 되면 어떻게 해요?
  coupon-faq 0.551 | delay-compensation 0.388 | cancel-policy 0.325 | refund-basic 0.315 | refund-after-delivered 0.284 | weather-delay 0.256 | privacy 0.246
Q: 제 번호 010-9876-5432로 라이더분이 도착 전에 연락 주시게 해주세요
  privacy 0.456 | weather-delay 0.392 | delay-compensation 0.323 | refund-basic 0.254 | cancel-policy 0.236 | refund-after-delivered 0.231 | coupon-faq 0.213
Q: 안녕하세요, 주문 문의드리려고요
  cancel-policy 0.338 | privacy 0.305 | refund-basic 0.303 | weather-delay 0.292 | refund-after-delivered 0.268 | delay-compensation 0.223 | coupon-faq 0.195
Q: 그 주문 지금 어디쯤이에요?
  cancel-policy 0.323 | refund-basic 0.276 | delay-compensation 0.251 | privacy 0.248 | weather-delay 0.240 | refund-after-delivered 0.215 | coupon-faq 0.193
Q: 주문번호 2024-1234 어떻게 됐어요?
  cancel-policy 0.389 | refund-basic 0.334 | privacy 0.294 | refund-after-delivered 0.291 | coupon-faq 0.285 | weather-delay 0.281 | delay-compensation 0.253
Q: 오늘 날씨 어때요?
  weather-delay 0.302 | delay-compensation 0.203 | privacy 0.201 | cancel-policy 0.154 | refund-basic 0.120 | refund-after-delivered 0.117 | coupon-faq 0.098
Q: 치킨 맛집 추천해줘
  coupon-faq 0.323 | refund-basic 0.260 | cancel-policy 0.230 | privacy 0.229 | refund-after-delivered 0.218 | delay-compensation 0.205 | weather-delay 0.146
Q: 사장님 번호 010-1234-5678 맞나요?
  privacy 0.375 | cancel-policy 0.242 | refund-basic 0.228 | delay-compensation 0.196 | coupon-faq 0.194 | refund-after-delivered 0.189 | weather-delay 0.154
Q: 비 오는 날 배달이 늦으면 보상 받을 수 있나요?
  delay-compensation 0.592 | weather-delay 0.525 | refund-after-delivered 0.409 | refund-basic 0.390 | coupon-faq 0.296 | cancel-policy 0.291 | privacy 0.176
# 아래 네 문장은 E2E 5/6/7/10턴 사용자 문장 단독 점수. E2E 로그 ragDocs 점수와 비교용
Q: 그럼 그 주문 취소해주세요
  cancel-policy 0.515 | refund-basic 0.406 | refund-after-delivered 0.374 | delay-compensation 0.319 | coupon-faq 0.292 | weather-delay 0.285 | privacy 0.277
Q: 다른 주문 2024-1239도 취소해주세요. 집에 사람이 없어요
  cancel-policy 0.519 | refund-basic 0.398 | privacy 0.395 | refund-after-delivered 0.367 | coupon-faq 0.354 | weather-delay 0.334 | delay-compensation 0.316
Q: 방금 그 주문 취소 한 번 더 해주세요
  cancel-policy 0.504 | refund-basic 0.418 | coupon-faq 0.394 | refund-after-delivered 0.390 | delay-compensation 0.354 | weather-delay 0.323 | privacy 0.285
Q: 제 번호 010-9876-5432로 라이더분이 도착 전에 연락 주시게 해주세요
  privacy 0.456 | weather-delay 0.392 | delay-compensation 0.323 | refund-basic 0.254 | cancel-policy 0.236 | refund-after-delivered 0.231 | coupon-faq 0.213
```

threshold 0.5에서는 첫 질문이 전부 탈락한다. 도메인 밖 / 주문 상태 질문의 최고점은 0.389(`주문번호 2024-1234 어떻게 됐어요?` → cancel-policy)다.
