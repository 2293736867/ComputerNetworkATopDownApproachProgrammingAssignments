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
        double minRtt = Double.MAX_VALUE;
        double maxRtt = 0.0;
        double totalRtt = 0.0;
        int lostPacket = 0;
        int n = 10;
        for (int i = 0; i < n; i++) {
            long sendTime = System.nanoTime();
            String message = "Ping " + (i + 1) + " " + sendTime;
            DatagramPacket packet = new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.getBytes(StandardCharsets.UTF_8).length);
            socket.send(packet);
            try {
                byte[] receivePacketBytes = new byte[1024];
                DatagramPacket receivePack = new DatagramPacket(receivePacketBytes, receivePacketBytes.length);
                socket.receive(receivePack);
                double rtt = (System.nanoTime() - sendTime) / (double) 1e9;
                if (rtt < minRtt) {
                    minRtt = rtt;
                }
                if (rtt > maxRtt) {
                    maxRtt = rtt;
                }
                totalRtt += rtt;
                System.out.printf("Sequence %d: Reply from %s RTT=%.8fs\n", i + 1, ip.getHostAddress(), rtt);
            } catch (Exception e) {
                ++lostPacket;
                System.out.println("Sequence " + (i + 1) + ": Request timed out");
            }
        }
        System.out.printf("max rtt is %.8fs\n", maxRtt);
        System.out.printf("min rtt is %.8fs\n", minRtt);
        System.out.printf("avg rtt is %.8fs\n", totalRtt / (n - lostPacket));
        System.out.println("lost radio is " + (lostPacket * 100 / n) + "%");
        socket.close();
    }
}
