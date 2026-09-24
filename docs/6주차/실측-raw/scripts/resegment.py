import re, sys
applog, md = sys.argv[1], sys.argv[2]
KEYS = ("[Turn]", "[Tool]", "LLM call", "[InputGuardrail]", "[OutputGuardrail]", "[Handoff]", "[Fallback]", "Executing tool call")
START = ("[Turn]", "[InputGuardrail]", "[Handoff]")
lines = [l.rstrip() for l in open(applog, encoding="utf-8", errors="replace") if any(k in l for k in KEYS)]
def short(l):
    m = re.match(r"\S+T(\d\d:\d\d:\d\d\.\d+)\S*\s+(\w+).*? : (.*)", l)
    return f"{m.group(1)} {m.group(2):5} {m.group(3)}" if m else l
segs = []
for l in lines:
    if any(k in l for k in START): segs.append([])
    if segs: segs[-1].append(short(l))
text = open(md, encoding="utf-8").read()
parts = re.split(r"(로그:\n\n```text\n)(.*?)(\n```)", text, flags=re.S)
# parts: [pre, head, body, tail, pre, ...]
out, i = [], 0
for j in range(0, len(parts) - 1, 4):
    out += [parts[j], parts[j+1], "\n".join(segs[i]) if i < len(segs) else "(없음)", parts[j+3]]; i += 1
out.append(parts[-1])
open(md, "w", encoding="utf-8").write("".join(out))
print("segments", len(segs))
