import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * BE1: Basic socket server with accept loop and client management
 */
public class Server {
    private final int port;
    private ServerSocket serverSocket;

    // Track active client handlers for broadcast
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private final Backend2Facade facade = new Backend2Facade();

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        while (true) {
            Socket socket = serverSocket.accept(); // blocking until client connects
            ClientHandler handler = new ClientHandler(socket, this, facade);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    public void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                try {
                    ch.send(msg);
                } catch (Exception ignored) {
                    // If sending fails, drop the client to keep set clean
                    removeClient(ch);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server(3000).start();
    }
}
