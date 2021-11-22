import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Server {
    public static void main(String[] args) throws Exception{
        DatagramSocket socket = new DatagramSocket(1234);
        Random random = new Random();
        while (true) {
            byte[] message = new byte[1024];
            DatagramPacket packet = new DatagramPacket(message, message.length);
            socket.receive(packet);
            if(random.nextInt(10) > 4) {
                packet.setData((message+" from server").getBytes(StandardCharsets.UTF_8));
                socket.send(packet);
            }
        }
    }
}
