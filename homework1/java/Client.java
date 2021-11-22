import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Please input complete arguments.");
            System.exit(0);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String filename = args[2];
        Socket socket = new Socket(host, port);
        try (InputStream inputStream = socket.getInputStream()) {
            try (OutputStream outputStream = socket.getOutputStream()) {
                var writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                writer.write("GET /" + filename + " HTTP/1.1\nHost:" + host + "\nConnection:close\n\n");
                writer.flush();
                boolean first = true;
                boolean meetEntity = false;
                StringBuilder builder = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    if (first) {
                        if (str.split(" ")[1].equals("200")) {
                            System.out.println("status code:200 OK");
                            first = false;
                        } else {
                            System.out.println("status code:404 Not Found");
                            break;
                        }
                    }
                    if (str.isBlank()) {
                        meetEntity = true;
                    }
                    if (meetEntity) {
                        builder.append(str);
                    }
                }
                if (meetEntity) {
                    Path path = Path.of("output.html");
                    System.out.println("please check output.html");
                    Files.write(path,builder.toString().getBytes(StandardCharsets.UTF_8));
                }
                reader.close();
            }
        }
    }
}
