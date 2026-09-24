# 같은 PgVector 테이블 위에서 (1) 임베딩 코사인 검색 (2) 단순 키워드 겹침 검색의 top-1 문서를 비교한다.
import json, subprocess, urllib.request
Q = [
 ("음식 받았는데 상했어요, 돈 돌려받을 수 있나요?", "refund-after-delivered"),
 ("라이더가 한참 안 와요. 뭐 해주는 거 있어요?", "delay-compensation"),
 ("태풍 때문에 늦는 것도 보상돼요?", "weather-delay"),
 ("할인권이 결제할 때 안 먹혀요", "coupon-faq"),
 ("제 연락처가 가게 사장님한테 넘어가나요?", "privacy"),
 ("요리 시작된 다음에도 주문 물릴 수 있어요?", "cancel-policy"),
 ("쿠폰 중복 사용 가능한가요?", "coupon-faq"),
 ("배달 지연 보상 기준 알려주세요", "delay-compensation"),
 ("환불 처리 기간은 얼마나 걸려요?", "refund-basic"),
 ("주문 취소 수수료 있나요?", "cancel-policy"),
]
def psql(sql):
    return subprocess.run(["docker","exec","-i","baedal-pgvector","psql","-U","baedal","-d","baedal","-At","-F","\t","-c",sql],
                          capture_output=True, text=True, check=True).stdout
def embed(t):
    req = urllib.request.Request("http://localhost:11434/api/embed", data=json.dumps({"model":"qwen3-embedding:0.6b","input":t}).encode(),
                                 headers={"Content-Type":"application/json"})
    return json.load(urllib.request.urlopen(req))["embeddings"][0]
rows = [l.split("\t",1) for l in psql("select metadata->>'faqId', replace(replace(content, E'\\n',' '), E'\\t',' ') from vector_store").splitlines()]
docs = {}
for fid, content in rows: docs[fid] = docs.get(fid, "") + " " + content
print(f"chunks={len(rows)} docs={len(docs)}")
res = []
for q, gold in Q:
    v = "[" + ",".join(map(str, embed(q))) + "]"
    top = psql(f"select metadata->>'faqId', round((1-(embedding <=> '{v}'))::numeric,3) from vector_store order by embedding <=> '{v}' limit 1").split("\t")
    toks = [t.strip("?,.!") for t in q.split() if len(t.strip("?,.!")) >= 2]
    scored = sorted(((sum(t in c for t in toks), fid) for fid, c in docs.items()), reverse=True)
    kw_score, kw_fid = scored[0]
    kw_fid = kw_fid if kw_score > 0 else "(없음)"
    tie = sum(1 for s, _ in scored if s == kw_score) > 1 and kw_score > 0
    r = {"query": q, "gold": gold, "embedding_top1": top[0], "cos": float(top[1]), "emb_ok": top[0]==gold,
         "keyword_top1": kw_fid + (" (동점)" if tie else ""), "keyword_hits": kw_score, "kw_ok": kw_fid==gold and not tie, "kw_gold_in_top": kw_score>0 and any(s==kw_score and f==gold for s,f in scored), "kw_gold_hits": dict((f,s) for s,f in scored)[gold], "tokens": toks}
    res.append(r); print(json.dumps(r, ensure_ascii=False))
print("keyword 동점 포함 정답", sum(r["kw_gold_in_top"] for r in res), "/", len(res))
print("embedding 정답", sum(r["emb_ok"] for r in res), "/", len(res), " keyword 정답", sum(r["kw_ok"] for r in res), "/", len(res))
