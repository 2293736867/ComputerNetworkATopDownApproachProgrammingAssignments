from socket import *
import sys

if len(sys.argv) < 3:
    print('Please input complete arguments.')
    sys.exit(0)
serverName = sys.argv[1]
serverPort = int(sys.argv[2])
filename = sys.argv[3]

s = socket(AF_INET,SOCK_STREAM)
s.connect((serverName,serverPort))
s.send(('GET /%s HTTP/1.1\nHost:127.0.0.1\nConnection:close\n\n'%filename).encode())

buffer = []
while True:
    d = s.recv(1024)
    if d:
        buffer.append(d)
    else:
        break
s.close()
data = b''.join(buffer)
try:
    header,html=data.split(b'\n\n',1)
except Exception:
    print('status code:404 Not Found')
    sys.exit(0)
print('status code:200 OK')
print('please check output.html')
with open('output.html','wb') as f:
    f.write(html)