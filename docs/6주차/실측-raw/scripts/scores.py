import json, os, subprocess, sys, urllib.request
def embed(t):
    r = urllib.request.Request("http://localhost:11434/api/embed", data=json.dumps({"model":os.environ.get("EMBED_MODEL","embeddinggemma"),"input":t}).encode(), headers={"Content-Type":"application/json"})
    return json.load(urllib.request.urlopen(r))["embeddings"][0]
for q in sys.argv[1:]:
    v = "[" + ",".join(map(str, embed(q))) + "]"
    out = subprocess.run(["docker","exec","-i","baedal-pgvector","psql","-U","baedal","-d","baedal","-At","-F"," ","-c",
        f"select metadata->>'faqId', round((1-(embedding <=> '{v}'))::numeric,3) from vector_store order by embedding <=> '{v}'"], capture_output=True, text=True).stdout
    print("Q:", q); print("  " + out.strip().replace("\n", " | "))
