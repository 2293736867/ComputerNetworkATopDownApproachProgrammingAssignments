import java.io.*;
import java.net.*;
import java.nio.file.*;

public class MultiThreadingProxyServer {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Please input proxy ip and port");
            System.out.println("Example:");
            System.out.println("java ***.java 127.0.0.1 1234");
            System.exit(0);
        }
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[1]), 50, InetAddress.getByName(args[0]));
        System.out.println("Server is runnning...");
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connected from " + socket.getRemoteSocketAddress());
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
                e.printStackTrace();
                try {
                    socket.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                System.out.println("Client disconnected");
            }
        }

        private void handle(InputStream input, OutputStream output) throws Exception {
            var writer = new BufferedOutputStream(output);
            var reader = new BufferedInputStream(input);
            byte[] buff = new byte[1024];
            reader.read(buff);
            String str = new String(buff, 0, buff.length);
            System.out.println("str is " + str);
            if (str.startsWith("GET")) {
                String url = str.substring(str.indexOf("GET") + 4, str.indexOf("HTTP") - 1);
                System.out.println("url is " + url);
                String filename = url.substring(url.lastIndexOf("/") + 1);
                System.out.println("filename is " + filename);
                Path path = Path.of(filename);
                if (Files.exists(path)) {
                    System.out.println("Read " + filename + " from cache");
                    byte[] bytes = Files.readAllBytes(path);
                    writer.write(bytes);
                    writer.flush();
                } else {
                    System.out.println("Cache not exists");
                    System.out.println("Creating socket on proxy server");
                    int hostIndex = str.indexOf("Host");
                    String host = str.substring(hostIndex + 6, str.indexOf("\r\n", hostIndex));
                    System.out.println("host is " + host);
                    Socket socket = new Socket(host, 80);
                    socket.setSoTimeout(10_000); // 10s
                    var socketOutput = socket.getOutputStream();
                    var socketInput = socket.getInputStream();
                    socketOutput.write(buff);
                    byte[] response = socketInput.readAllBytes();
                    String strResponse = new String(response, 0, response.length);
                    System.out.println("Response is " + strResponse);
                    int code = Integer.parseInt(strResponse.split(" ")[1]);
                    System.out.println("Status code :" + code);
                    if (code == 200) {
                        Path tempFile = Path.of(filename);
                        Files.write(tempFile, response);
                        System.out.println("Cache file:" + filename);
                    }
                    writer.write(response);
                    writer.flush();
                }
            } else if (str.startsWith("POST")) {
                int hostIndex = str.indexOf("Host");
                String host = str.substring(hostIndex, str.indexOf("\r\n", hostIndex)).substring(6);
                int port = Integer.parseInt(host.split(":")[1]);
                host = host.split(":")[0];
                Socket socket = new Socket(host, port);
                socket.setSoTimeout(10_000); //10s
                socket.getOutputStream().write(buff);
                byte[] response = socket.getInputStream().readAllBytes();
                int code = Integer.parseInt(new String(response).split(" ")[1]);
                System.out.println("status code :" + code);
                writer.write(response);
                writer.flush();
            } else {
                throw new Exception("Method not supported");
            }
        }
    }
}
