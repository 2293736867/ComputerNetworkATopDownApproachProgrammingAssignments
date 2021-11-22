import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Server {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("server is runnning...");
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("connected from " + socket.getRemoteSocketAddress());
            new Handler(socket).start();
        }
    }

    private static class Handler extends Thread {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (InputStream inputStream = socket.getInputStream()) {
                try (OutputStream outputStream = socket.getOutputStream()) {
                    handle(inputStream, outputStream);
                }
            } catch (Exception e) {
                try {
                    socket.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                System.out.println("client disconnected");
            }
        }

        private void handle(InputStream input, OutputStream output) throws Exception {
            var writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
            var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            Path path = Path.of(reader.readLine().split(" ")[1].substring(1));
            if (Files.exists(path)) {
                String header = "HTTP/1.1 200 OK\nConnection:close\nContent-Type:text/html;Content-Length:" + Files.size(path) + "\n\n";
                writer.write(header);
                writer.write(String.join("", Files.readAllLines(path)));
            } else {
                String header = "HTTP/1.1 404 Not Found\n";
                writer.write(header);
            }
            writer.flush();
            writer.close();
        }
    }
}
