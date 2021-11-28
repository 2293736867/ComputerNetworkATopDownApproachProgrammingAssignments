from socket import *
import base64

def check(code):
    recv = clientSocket.recv(1024).decode()
    print(recv)
    if recv[:3] == str(code):
        print("response success")
    else:
        print("response failed:%d"%code)

def send(message,code):
    clientSocket.sendall((message+'\r\n').encode())
    print('------------send message:%s-----------'% message)
    print('-------------------response-------------------')
    check(code)
    print('-------------------finish--------------------')
    print()

def append(filed,body):
    global message
    message += filed+":"+body+'\r\n'

def appendFrom(appendStr):
    append("from",appendStr)

def appendTo(appendStr):
    append("to",appendStr)

def appendSubject(appendStr):
    append("subject",appendStr)

def appendContentType(contentType):
    append("Content-Type",contentType)
    if contentType.split("/")[0] == 'image':
        append('Content-transfer-encoding','base64')

def appendSimple(contentType,body):
    global message
    message += "--simple\r\n"
    if len(contentType) != 0:
        appendContentType(contentType)
        message += '\r\n' + body + '\r\n'

mailServer = "smtp.qq.com"
fromAddress = "*******@qq.com" # 发送者邮箱
toAddress = "*********@qq.com" # 接收者邮箱
subject = "I love computer networks"
qq_mail_authentication_code = "*******" # qq邮箱授权码，可以到自己的qq邮箱中去获取
username = str(base64.b64encode(fromAddress.encode()),encoding="utf-8")
password = str(base64.b64encode(qq_mail_authentication_code.encode()),encoding="utf-8")

clientSocket = socket(AF_INET, SOCK_STREAM)
clientSocket.connect((mailServer, 587)) # TLS port 587   SMTP port 25
check(220)

send('HELO Alice',250)
send('AUTH LOGIN',334)
send(username,334)
send(password,235)
send('MAIL FROM:<'+fromAddress+'>',250)
send('RCPT TO:<'+toAddress+'>',250)
send('DATA',354)

message = ''
appendFrom(fromAddress)
appendTo(toAddress)
appendSubject(subject)
appendContentType('multipart/mixed;boundary="simple"')
appendSimple('text/html','<h1>hello</h1><img src="https://pic3.zhimg.com/50/v2-29a01fdecc80b16e73160c40637a5e8c_hd.jpg">')
appendSimple('image/png', str(base64.b64encode(open('test.png','rb').read()),encoding="utf-8")) # test.png 测试图片
appendSimple('','')

clientSocket.sendall(message.encode())
clientSocket.sendall('\r\n.\r\n'.encode())
check(250)

clientSocket.sendall('QUIT\r\n'.encode())
clientSocket.close()
