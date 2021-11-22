import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(1235);
        socket.setSoTimeout(1000);
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        int port = 1234;
        socket.connect(ip, port);
        int n = 10;
        for (int i = 0; i < n; i++) {
            String message = (i + 1) + " " + System.nanoTime();
            DatagramPacket packet = new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.getBytes(StandardCharsets.UTF_8).length);
            socket.send(packet);
        }
        socket.close();
    }
}
