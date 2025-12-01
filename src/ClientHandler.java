import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Using default package classes

/**
 * BE1: Per-connection handler; parses basic protocol and interacts with BE2
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final Backend2Facade facade;
    private PrintWriter out;
    private BufferedReader in;
    private String username = "";

    public ClientHandler(Socket socket, Server server, Backend2Facade facade) {
        this.socket = socket;
        this.server = server;
        this.facade = facade;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                handle(line);
            }
        } catch (IOException e) {
            // connection closed or error
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    private void handle(String msg) {
        try {
            // Handle simplified LOGIN without password for FE handshake
            if (msg.startsWith("LOGIN|") && msg.split("\\|").length == 2) {
                username = msg.substring("LOGIN|".length());
                send("LOGIN_OK|Selamat datang " + username);
                return;
            }

            // Use BE2 facade to process text commands and emit responses
            String response = facade.handleTextCommand(msg);
            if (response != null && !response.isEmpty()) {
                send(response);
            }

            // Report streaming: REQ_REPORT -> FILE_SIZE, wait READY_TO_RECEIVE -> stream
            // bytes
            if ("REQ_REPORT".equalsIgnoreCase(msg)) {
                // facade already returned FILE_SIZE header above
                String ready = in.readLine();
                if ("READY_TO_RECEIVE".equalsIgnoreCase(ready)) {
                    facade.writeReportToStream(socket.getOutputStream());
                }
                return;
            }

            // Trigger broadcasts on data-changing commands
            if (msg.startsWith("ADD|") || msg.startsWith("UPDATE|") || msg.startsWith("DELETE|")) {
                server.broadcast("REFRESH_TABLE");
                String summary = facade.handleTextCommand("GET_SUMMARY");
                server.broadcast(summary);
            }
        } catch (Exception e) {
            send("ERROR|" + e.getMessage());
        }
    }
}
