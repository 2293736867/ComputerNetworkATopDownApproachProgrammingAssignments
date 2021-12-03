from socket import *
import sys
import os
import threading

def tcplink(clientSocket,address):
    print('Received a connection from ',address)
    message = clientSocket.recv(1024).decode()
    if message.startswith("GET"): # GET Method
        print('Message is ',message)
        filename = message.split()[1].partition("/")[2].replace('/', '_')
        print('Filename is ',filename)

        if os.path.exists(filename):
            print('Read %s from cache'%filename)
            f = open(filename,"r")
            outputData = f.readlines()
            for i in range(0,len(outputData)):
                clientSocket.send(outputData[i].encode())
        else: # Cache missed
            print('Cache not exists')
            print('Creating socket on proxy server')
            c = socket(AF_INET,SOCK_STREAM)
            c.settimeout(10)
            host = message.split()[1].partition("//")[2].partition("/")[0]
            try:
                c.connect((host,80))
                print('Socket connected to %s:80'%host)
                c.sendall(message.encode())
                buff = c.recv(1024)
                code = buff.decode().split()[1]
                if code == '200': # status code 200 -> cache response
                    tempFile = open("./"+filename,"w")
                    tempFile.writelines(buff.decode().replace('\r\n', '\n'))
                    tempFile.close()
                    print('Cache file:',filename)
                print('Status code:',code)
                clientSocket.sendall(buff)
            except Exception as e:
                print(e)
    elif message.startswith("POST"): # POST method
        temp = message.split('\r\n')
        host = ''
        for i in range(0,len(temp)):
            if temp[i].startswith('Host'):
                host = temp[i].split(" ")[1]
                break
        port = host.split(':')[1]
        host = host.split(':')[0]
        try:
            c = socket(AF_INET,SOCK_STREAM)
            c.settimeout(10)
            c.connect((host,int(port)))
            print('Socket connected to %s:%s'%(host,port))
            c.sendall(message.encode())
            buff = c.recv(1024)
            code = buff.decode().split()[1]
            print('Status code:',code)
            clientSocket.send(buff)
        except Exception as e:
            print(e)
    else:
        print('Method not supported')
    clientSocket.close()

if len(sys.argv) < 3:
    print('Please input proxy ip and port.')
    print('Example:')
    print('python XXX.py 127.0.0.1 1234')
    sys.exit(2)

serverSocket = socket(AF_INET,SOCK_STREAM)
serverSocket.bind((sys.argv[1],int(sys.argv[2])))
serverSocket.listen(5)

while True:
    print('Ready to serve...')
    clientSocket,address = serverSocket.accept()
    t = threading.Thread(target=tcplink,args=(clientSocket,address))
    t.start()
serverSocket.close()