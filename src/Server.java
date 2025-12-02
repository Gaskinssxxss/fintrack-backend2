import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BE1: Basic socket server with accept loop and client management
 */
public class Server {
    private final int port;
    private ServerSocket serverSocket;

    // Track active client handlers for broadcast
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    // Per-user subscriber registry for realtime updates
    private final Map<String, Set<ClientHandler>> userSubscribers = new ConcurrentHashMap<>();
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
        // Also remove from any user subscription lists
        synchronized (userSubscribers) {
            for (Set<ClientHandler> set : userSubscribers.values()) {
                set.remove(handler);
            }
        }
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

    // Register a handler as subscriber for a specific username
    public void subscribe(String username, ClientHandler handler) {
        if (username == null || username.isEmpty())
            return;
        userSubscribers.computeIfAbsent(username, k -> Collections.synchronizedSet(new HashSet<>()))
                .add(handler);
    }

    // Unsubscribe a handler from a specific username
    public void unsubscribe(String username, ClientHandler handler) {
        if (username == null || username.isEmpty())
            return;
        Set<ClientHandler> set = userSubscribers.get(username);
        if (set != null) {
            set.remove(handler);
            if (set.isEmpty()) {
                userSubscribers.remove(username);
            }
        }
    }

    // Send an event message to all subscribers of a user
    public void sendToUser(String username, String msg) {
        if (username == null || username.isEmpty())
            return;
        Set<ClientHandler> set = userSubscribers.get(username);
        if (set == null)
            return;
        synchronized (set) {
            Iterator<ClientHandler> it = set.iterator();
            while (it.hasNext()) {
                ClientHandler ch = it.next();
                try {
                    ch.send(msg);
                } catch (Exception e) {
                    it.remove();
                    clients.remove(ch);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server(3000).start();
    }
}
