import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private static final String FILE_NAME = "data/users.txt";

    private final Map<String, User> users = new HashMap<>();

    public UserManager() {
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    User u = User.fromCSV(line);
                    users.put(u.getUsername(), u);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ====== CREATE ======

    public synchronized boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }

        User u = new User(username, password);
        users.put(username, u);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME, true), "UTF-8"))) {

            writer.write(u.toCSV());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    // ====== READ ======

    public synchronized boolean validateLogin(String username, String password) {
        User u = users.get(username);
        if (u == null) return false;
        return u.getPassword().equals(password);
    }

    public synchronized User getUser(String username) {
        return users.get(username);
    }

    // ====== UPDATE ======

    public synchronized boolean changePassword(String username,
                                               String oldPassword,
                                               String newPassword) {
        User u = users.get(username);
        if (u == null) {
            return false;
        }
        if (!u.getPassword().equals(oldPassword)) {
            return false;
        }

        User updated = new User(username, newPassword);
        users.put(username, updated);

        saveAllUsersToFile();
        return true;
    }

    private void saveAllUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME, false), "UTF-8"))) {

            for (User u : users.values()) {
                writer.write(u.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
