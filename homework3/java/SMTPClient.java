import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class SMTPClient {
    public static void main(String[] args) throws Exception {
        new SMTPClient();
    }

    private BufferedWriter writer;
    private BufferedReader reader;
    private final StringBuilder message = new StringBuilder();

    public SMTPClient() throws Exception {
        final boolean ENABLE_SSL = true;
        final String MAIL_SERVER = "smtp.qq.com";
        final String FROM_ADDRESS = "******@qq.com"; // 发送者邮箱
        final String TO_ADDRESS = "*****@qq.com"; // 接收者邮箱
        final String USERNAME = Base64.getEncoder().encodeToString(FROM_ADDRESS.getBytes(StandardCharsets.UTF_8));
        final String QQ_MAIL_AUTHENTICATION_CODE = "********"; //qq邮箱授权码
        final String PASSWORD = Base64.getEncoder().encodeToString(QQ_MAIL_AUTHENTICATION_CODE.getBytes(StandardCharsets.UTF_8));
        final String SUBJECT = "I love computer networks.";
        Socket socket = new Socket(MAIL_SERVER, ENABLE_SSL ? 587 : 25);
        try (InputStream inputStream = socket.getInputStream()) {
            try (OutputStream outputStream = socket.getOutputStream()) {
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                check(220);
                send("HELO Alice",250);
                send("AUTH LOGIN",334);
                send(USERNAME,334);
                send(PASSWORD,235);
                send("MAIL FROM:<"+ FROM_ADDRESS +">",250);
                send("RCPT TO:<"+ TO_ADDRESS +">",250);
                send("DATA",354);

                appendFrom(FROM_ADDRESS);
                appendTo(TO_ADDRESS);
                appendSubject(SUBJECT);
                appendContentType("multipart/mixed;boundary=\"simple\"");
                appendSimple("text/html","<h1>hello</h1><img src=\"https://pic3.zhimg.com/50/v2-29a01fdecc80b16e73160c40637a5e8c_hd.jpg\">");
                appendSimple("image/png",Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of("test.png"))));
                appendSimple("","");
                writer.write(message.toString());
                writer.write("\r\n.\r\n");
                writer.flush();
                check(250);
                writer.write("QUIT\r\n");
                writer.flush();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void check(int code) throws Exception{
        char[] str = new char[1024];
        reader.read(str);
        int cur = Integer.parseInt(new String(str,0,3));
        if (cur == code) {
            System.out.println("response success");
        } else {
            System.out.println("response failed:" + cur);
        }
    }

    private void send(String message, int code) throws Exception {
        writer.write(message+"\r\n");
        writer.flush();
        System.out.println("send message:"+message);
        System.out.println("----------------response-------------");
        check(code);
        System.out.println("----------------finish-------------");
        System.out.println();
    }

    private void append(String field,String body){
        message.append(field).append(":").append(body).append("\r\n");
    }

    private void appendFrom(String from){
        append("from",from);
    }

    private void appendTo(String to){
        append("to",to);
    }

    private void appendSubject(String subject){
        append("subject",subject);
    }

    private void appendContentType(String contentType){
        append("Content-Type",contentType);
        if(contentType.split("/")[0].equals("image")){
            append("Content-transfer-encoding","base64");
        }
    }

    private void appendSimple(String contentType,String body){
        message.append("--simple").append("\r\n");
        if(contentType.length() != 0){
            appendContentType(contentType);
            message.append("\r\n").append(body).append("\r\n");
        }
    }
}
