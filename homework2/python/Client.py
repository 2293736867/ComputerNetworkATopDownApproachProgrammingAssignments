from socket import *
import time
import sys

serverName = '127.0.0.1'
serverPort = 1234
clientSocket = socket(AF_INET,SOCK_DGRAM)
clientSocket.settimeout(1)
minRtt = sys.maxsize
maxRtt = 0
totalRtt = 0
lostPacket = 0
n = 10

for i in range(0,n):
    sendTime = time.time()
    message = ('Ping %d %s '%(i+1,sendTime)).encode()
    try:
        clientSocket.sendto(message, (serverName,serverPort))
        modifiedMessage,serverAddress = clientSocket.recvfrom(1024)
        rtt = time.time() - sendTime
        if rtt < minRtt:
            minRtt = rtt
        if rtt > maxRtt:
            maxRtt = rtt
        totalRtt += rtt
        print('Sequence %d: Reply from %s   RTT=%.8fs' % (i+1,serverName,rtt))
    except Exception as e:
        print('Sequence %d: Request timed out'%(i+1))
        lostPacket = lostPacket+1

print('max rtt is %.8fs'%maxRtt)
print('min rtt is %.8fs'%minRtt)
print('avg rtt is %.8fs'%(totalRtt/(n-lostPacket)))
print('lost radio is %d%%'%(lostPacket*100/n))
clientSocket.close()