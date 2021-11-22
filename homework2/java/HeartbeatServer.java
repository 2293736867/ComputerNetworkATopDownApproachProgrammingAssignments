import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Server {
    public static void main(String[] args) throws Exception{
        DatagramSocket socket = new DatagramSocket(1234);
        socket.setSoTimeout(100);
        Random random = new Random();
        long startTime = System.nanoTime();
        long endTime = startTime;
        while (true) {
            try {
                byte[] message = new byte[1024];
                DatagramPacket packet = new DatagramPacket(message, message.length);
                socket.receive(packet);
                String str = new String(packet.getData(), packet.getOffset(), packet.getLength());
                endTime = Long.parseLong(str.split(" ")[1]);
                System.out.println(str.split(" ")[0]+" "+(endTime-startTime));
            }catch (Exception e){
                if(startTime == endTime){
                    continue;
                }
                if(System.nanoTime() - endTime > 1_000_000_000){ // 1s
                    System.out.println("Heartbeat pause");
                    break;
                }else{
                    System.out.println("Packet lost");
                }
            }
        }
    }
}