# 단계5 raw — 키워드 겹침 검색 vs 임베딩 검색 top-1

실행일: 2026-09-25. 같은 `vector_store` 7개 청크 위에서 비교했다.

- 임베딩: 질문을 `qwen3-embedding:0.6b`로 임베딩 → pgvector 코사인 거리 1위 문서
- 키워드: 질문을 공백으로 자르고 문장부호를 뗀 2글자 이상 토큰이, 문서 본문에 부분 문자열로 몇 개 들어 있는지 세서 1위 문서. 형태소 분석/BM25 없음
- `kw_ok`는 1위가 정답 하나로 좁혀졌을 때만 true, `kw_gold_in_top`은 동점 1위 안에 정답이 있으면 true

재현: `EMBED_MODEL=qwen3-embedding:0.6b python3 docs/6주차/실측-raw/scripts/kw_vs_emb.py` (당시 vector_store도 qwen 임베딩 1024차원). embeddinggemma 결과는 `docs/6주차/실측-raw/embeddinggemma-재측정.md`

```text
chunks=7 docs=7
{"query": "음식 받았는데 상했어요, 돈 돌려받을 수 있나요?", "gold": "refund-after-delivered", "embedding_top1": "refund-after-delivered", "cos": 0.434, "emb_ok": true, "keyword_top1": "refund-basic (동점)", "keyword_hits": 1, "kw_ok": false, "kw_gold_in_top": true, "kw_gold_hits": 1, "tokens": ["음식", "받았는데", "상했어요", "돌려받을", "있나요"]}
{"query": "라이더가 한참 안 와요. 뭐 해주는 거 있어요?", "gold": "delay-compensation", "embedding_top1": "weather-delay", "cos": 0.307, "emb_ok": false, "keyword_top1": "weather-delay", "keyword_hits": 1, "kw_ok": false, "kw_gold_in_top": false, "kw_gold_hits": 0, "tokens": ["라이더가", "한참", "와요", "해주는", "있어요"]}
{"query": "태풍 때문에 늦는 것도 보상돼요?", "gold": "weather-delay", "embedding_top1": "weather-delay", "cos": 0.542, "emb_ok": true, "keyword_top1": "weather-delay (동점)", "keyword_hits": 1, "kw_ok": false, "kw_gold_in_top": true, "kw_gold_hits": 1, "tokens": ["태풍", "때문에", "늦는", "것도", "보상돼요"]}
{"query": "할인권이 결제할 때 안 먹혀요", "gold": "coupon-faq", "embedding_top1": "coupon-faq", "cos": 0.469, "emb_ok": true, "keyword_top1": "(없음)", "keyword_hits": 0, "kw_ok": false, "kw_gold_in_top": false, "kw_gold_hits": 0, "tokens": ["할인권이", "결제할", "먹혀요"]}
{"query": "제 연락처가 가게 사장님한테 넘어가나요?", "gold": "privacy", "embedding_top1": "privacy", "cos": 0.352, "emb_ok": true, "keyword_top1": "refund-basic (동점)", "keyword_hits": 1, "kw_ok": false, "kw_gold_in_top": true, "kw_gold_hits": 1, "tokens": ["연락처가", "가게", "사장님한테", "넘어가나요"]}
{"query": "요리 시작된 다음에도 주문 물릴 수 있어요?", "gold": "cancel-policy", "embedding_top1": "cancel-policy", "cos": 0.475, "emb_ok": true, "keyword_top1": "refund-basic (동점)", "keyword_hits": 2, "kw_ok": false, "kw_gold_in_top": true, "kw_gold_hits": 2, "tokens": ["요리", "시작된", "다음에도", "주문", "물릴", "있어요"]}
{"query": "쿠폰 중복 사용 가능한가요?", "gold": "coupon-faq", "embedding_top1": "coupon-faq", "cos": 0.65, "emb_ok": true, "keyword_top1": "coupon-faq", "keyword_hits": 3, "kw_ok": true, "kw_gold_in_top": true, "kw_gold_hits": 3, "tokens": ["쿠폰", "중복", "사용", "가능한가요"]}
{"query": "배달 지연 보상 기준 알려주세요", "gold": "delay-compensation", "embedding_top1": "delay-compensation", "cos": 0.773, "emb_ok": true, "keyword_top1": "weather-delay (동점)", "keyword_hits": 4, "kw_ok": false, "kw_gold_in_top": true, "kw_gold_hits": 4, "tokens": ["배달", "지연", "보상", "기준", "알려주세요"]}
{"query": "환불 처리 기간은 얼마나 걸려요?", "gold": "refund-basic", "embedding_top1": "refund-basic", "cos": 0.486, "emb_ok": true, "keyword_top1": "refund-basic (동점)", "keyword_hits": 2, "kw_ok": false, "kw_gold_in_top": true, "kw_gold_hits": 2, "tokens": ["환불", "처리", "기간은", "얼마나", "걸려요"]}
{"query": "주문 취소 수수료 있나요?", "gold": "cancel-policy", "embedding_top1": "cancel-policy", "cos": 0.5, "emb_ok": true, "keyword_top1": "coupon-faq", "keyword_hits": 3, "kw_ok": false, "kw_gold_in_top": false, "kw_gold_hits": 2, "tokens": ["주문", "취소", "수수료", "있나요"]}
keyword 동점 포함 정답 7 / 10
embedding 정답 9 / 10  keyword 정답 1 / 10
```
