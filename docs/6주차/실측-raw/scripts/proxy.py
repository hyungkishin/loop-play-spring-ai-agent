# 11435 -> 11434 TCP 포워더. 이 프로세스를 죽이면 앱 입장에서 Ollama가 사라진다(실제 Ollama는 그대로).
import socket, threading
def pipe(a, b):
    try:
        while (d := a.recv(65536)): b.sendall(d)
    except OSError: pass
    finally:
        for s in (a, b):
            try: s.shutdown(socket.SHUT_RDWR)
            except OSError: pass
srv = socket.socket(); srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1); srv.bind(("127.0.0.1", 11435)); srv.listen(64)
while True:
    c, _ = srv.accept(); u = socket.create_connection(("127.0.0.1", 11434))
    threading.Thread(target=pipe, args=(c, u), daemon=True).start(); threading.Thread(target=pipe, args=(u, c), daemon=True).start()
