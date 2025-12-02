package service;

import model.User;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private static final String FILE_NAME = "data/users.txt";

    private final Map<String, User> users = new HashMap<>();

    public UserManager() {
        loadUsersFromFile();
        System.out.println("[UserManager] Loaded " + users.size() + " users");
        for (String username : users.keySet()) {
            System.out.println("[UserManager]   - " + username);
        }
    }

    private void loadUsersFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("[UserManager] Users file not found: " + file.getAbsolutePath());
            return;
        }

        System.out.println("[UserManager] Loading users from: " + file.getAbsolutePath());

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
            System.err.println("[UserManager] Error loading users: " + e.getMessage());
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
        System.out.println("[UserManager] validateLogin: username='" + username + "', password='" + password + "'");
        User u = users.get(username);
        if (u == null) {
            System.out.println("[UserManager] User not found: " + username);
            return false;
        }
        boolean matches = u.getPassword().equals(password);
        System.out.println("[UserManager] Password match: " + matches + " (expected: '" + u.getPassword() + "')");
        return matches;
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
