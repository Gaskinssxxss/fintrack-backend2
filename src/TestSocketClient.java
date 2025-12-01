import java.io.*;
import java.net.Socket;

public class TestSocketClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = 3000;
        try (Socket sock = new Socket(host, port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

            // Handshake login (simple)
            out.println("LOGIN|tester");
            System.out.println("SENT: LOGIN|tester");
            System.out.println("RECV: " + in.readLine());

            // Get summary (may be empty at start)
            out.println("GET_SUMMARY");
            System.out.println("SENT: GET_SUMMARY");
            System.out.println("RECV: " + in.readLine());

            // Add one transaction via BE2 format requires username
            out.println("ADD|tester|2025-12-02|Sample Desc|General|INCOME|10000");
            System.out.println("SENT: ADD|tester|...");
            System.out.println("RECV: " + in.readLine()); // SUMMARY

            // Expect server broadcasts: REFRESH_TABLE and SUMMARY; read two lines
            System.out.println("RECV(broadcast): " + in.readLine());
            System.out.println("RECV(broadcast): " + in.readLine());

            // Get all
            out.println("GET_ALL");
            System.out.println("SENT: GET_ALL");
            System.out.println("RECV: " + in.readLine());

            // Request report size and then receive bytes
            out.println("REQ_REPORT");
            System.out.println("SENT: REQ_REPORT");
            String header = in.readLine();
            System.out.println("RECV: " + header);
            if (header != null && header.startsWith("FILE_SIZE|")) {
                out.println("READY_TO_RECEIVE");
                int size = Integer.parseInt(header.split("\\|")[1]);
                try (FileOutputStream fos = new FileOutputStream("report_test_client.csv")) {
                    InputStream raw = sock.getInputStream();
                    byte[] buf = new byte[4096];
                    int remaining = size;
                    while (remaining > 0) {
                        int read = raw.read(buf, 0, Math.min(buf.length, remaining));
                        if (read == -1)
                            break;
                        fos.write(buf, 0, read);
                        remaining -= read;
                    }
                    System.out.println("Downloaded bytes: " + (size - remaining));
                }
            }
        }
    }
}
