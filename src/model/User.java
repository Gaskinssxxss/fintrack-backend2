import java.io.Serializable;

public class User implements Serializable {

    private String username;
    private String password; // disimpan plain text untuk demo

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Format penyimpanan di file: username|password
    public String toCSV() {
        return username + "|" + password;
    }

    public static User fromCSV(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format user tidak valid: " + line);
        }
        return new User(parts[0], parts[1]);
    }
}
