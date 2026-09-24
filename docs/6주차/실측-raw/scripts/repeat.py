# usage: repeat.py APPLOG OUT.jsonl  (cases from stdin json: [{"id","endpoint","message","n","markers":[...]}])
import json, sys, time, urllib.request, uuid
BASE = "http://127.0.0.1:18080"
APPLOG, OUT = sys.argv[1], sys.argv[2]
KEYS = ("[Turn]", "[Tool]", "LLM call", "[InputGuardrail]", "[OutputGuardrail]", "[Handoff]", "[Fallback]")
def lines():
    with open(APPLOG, encoding="utf-8", errors="replace") as f: return f.readlines()
cases = json.load(sys.stdin)
with open(OUT, "a", encoding="utf-8") as out:
    for c in cases:
        for k in range(1, c["n"] + 1):
            before = len(lines()); sid = f"{c['id']}-{k}-{uuid.uuid4().hex[:6]}"
            req = urllib.request.Request(BASE + c["endpoint"], data=json.dumps({"message": c["message"]}).encode(),
                headers={"Content-Type": "application/json", "X-Session-Id": sid, "X-Forwarded-For": f"198.51.100.{k}"})
            t0 = time.time()
            try:
                with urllib.request.urlopen(req, timeout=300) as r: status, body = r.status, r.read().decode()
            except urllib.error.HTTPError as e: status, body = e.code, e.read().decode()
            ms = int((time.time() - t0) * 1000); time.sleep(0.3)
            logs = [l.rstrip().split(" : ", 1)[-1] for l in lines()[before:] if any(x in l for x in KEYS)]
            rec = {"case": c["id"], "run": k, "endpoint": c["endpoint"], "message": c["message"], "status": status, "ms": ms,
                   "markers": {m: (m in body) for m in c.get("markers", [])}, "body": body, "logs": logs}
            out.write(json.dumps(rec, ensure_ascii=False) + "\n"); out.flush()
            print(c["id"], k, status, ms, rec["markers"], body[:70].replace("\n", " "), flush=True)
