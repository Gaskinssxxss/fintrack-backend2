import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import util.RequestLogger;

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
        RequestLogger logger = RequestLogger.getInstance();
        String command = msg.split("\\|")[0];

        try {
            // Log incoming request
            logger.logRequest(msg, username.isEmpty() ? "ANONYMOUS" : username);

            // SUBSCRIBE|sessionToken -> register this handler to user updates
            if (msg.startsWith("SUBSCRIBE|") && msg.split("\\|").length == 2) {
                String token = msg.substring("SUBSCRIBE|".length());
                String resolvedUser = facade.getUsernameForSession(token);
                if (resolvedUser != null) {
                    this.username = resolvedUser;
                    server.subscribe(resolvedUser, this);
                    send("SUBSCRIBED|" + resolvedUser);
                    logger.logSuccess(command, "Subscribed: " + resolvedUser);
                } else {
                    send("ERROR|SESSION_INVALID");
                    logger.logError(command, "Subscribe failed: invalid session");
                }
                return;
            }

            // Use BE2 facade to process text commands and emit responses
            String response = facade.handleTextCommand(msg);
            if (response != null && !response.isEmpty()) {
                send(response);

                // Log response status
                if (response.startsWith("OK") || response.startsWith("LOGIN_OK") ||
                        response.startsWith("SUMMARY") || response.startsWith("DATA_ALL") ||
                        response.startsWith("FILE_SIZE")) {
                    logger.logSuccess(command, "Response: " + response.substring(0, Math.min(100, response.length())));
                } else if (response.startsWith("ERROR")) {
                    logger.logError(command, response);
                }
            }

            // Report streaming: REQ_REPORT/REQ_DOWNLOAD -> FILE_SIZE, wait READY_TO_RECEIVE
            // -> stream
            // bytes
            if ("REQ_REPORT".equalsIgnoreCase(msg) || "REQ_DOWNLOAD".equalsIgnoreCase(msg)) {
                // facade already returned FILE_SIZE header above
                String ready = in.readLine();
                if ("READY_TO_RECEIVE".equalsIgnoreCase(ready)) {
                    facade.writeReportToStream(socket.getOutputStream());
                    logger.logSuccess(command, "Report streamed successfully");
                }
                return;
            }

            // Trigger per-user event broadcasts on data-changing commands to subscribers
            if (msg.startsWith("ADD|") || msg.startsWith("UPDATE|") || msg.startsWith("DELETE|") ||
                    msg.startsWith("ADD_ACCOUNT|") || msg.startsWith("UPDATE_ACCOUNT|")
                    || msg.startsWith("DELETE_ACCOUNT|") ||
                    msg.startsWith("ADD_CATEGORY|") || msg.startsWith("DELETE_CATEGORY|")) {
                // Resolve username from session token (2nd field)
                String[] parts = msg.split("\\|");
                if (parts.length >= 2) {
                    String token = parts[1];
                    String user = facade.getUsernameForSession(token);
                    if (user != null) {
                        // Lightweight event; clients will fetch fresh state
                        server.sendToUser(user, "EVENT|DATA_CHANGED");
                    }
                }
            }
        } catch (Exception e) {
            send("ERROR|" + e.getMessage());
            logger.logError(command, "Exception: " + e.getMessage());
        }
    }
}
