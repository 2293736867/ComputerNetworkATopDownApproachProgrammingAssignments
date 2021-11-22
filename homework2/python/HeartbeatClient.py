from socket import *
import time

serverName = '127.0.0.1'
serverPort = 1234
clientSocket = socket(AF_INET,SOCK_DGRAM)
clientSocket.settimeout(1)
n = 10

for i in range(0,n):
    message = ('%d %s'%(i+1,time.time())).encode()
    clientSocket.sendto(message, (serverName,serverPort))
    
clientSocket.close()