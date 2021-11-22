from socket import *
import random
import time

serverSocket = socket(AF_INET,SOCK_DGRAM)
serverSocket.bind(('127.0.0.1',1234))

startTime = float(time.time())
endTime = startTime

while True:
    try:
        serverSocket.settimeout(0.1)
        message,address = serverSocket.recvfrom(1024)
        message = message.decode()
        rtime = float(message.split()[1])
        endTime = rtime
        ping = float(time.time())-rtime
        print('%s:%.8f'%(message.split()[0],ping))
    except Exception as e:
        if endTime == startTime:
            continue
        if time.time() - endTime>=1.0:
            print('Hearbeat pause')
            break
        else:
            print('Packet lost')