from socket import *
import threading

def tcplink(sock,addr):
    print('accept new connection from %s:%s...' % addr)
    try:
        message = sock.recv(1024)
        filename = message.split()[1]
        f = open(filename[1:])
        output_data = f.read()
        header = 'HTTP/1.1 200 OK\nConnection:close\nContent-Type:text/html\nContent-Length:%d\n\n' % (len(output_data))
        sock.send(header.encode())

        for i in range(0,len(output_data)):
            sock.send(output_data[i].encode())
        sock.close()
    except IOError:
        header = 'HTTP/1.1 404 Not Found\n'
        sock.send(header.encode())
        sock.close()

serverSocket = socket(AF_INET,SOCK_STREAM)
serverSocket.bind(('127.0.0.1',1234))
serverSocket.listen(5)

while True:
    sock,addr = serverSocket.accept()
    t = threading.Thread(target=tcplink,args=(sock,addr))
    t.start()