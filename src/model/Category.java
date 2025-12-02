package model;

import java.io.Serializable;

public class Category implements Serializable {

    // Constants for category types
    public static final String TYPE_INCOME = "Pemasukan";
    public static final String TYPE_EXPENSE = "Pengeluaran";

    private String username;    // Username pemilik kategori
    private String type;        // "Pemasukan" atau "Pengeluaran"
    private String name;        // Nama kategori (e.g., "Makanan & Minuman")

    public Category(String username, String type, String name) {
        this.username = username;
        this.type = type;
        this.name = name;
    }

    // Dipakai saat simpan ke file (Stream)
    public String toCSV() {
        return username + "|" + type + "|" + name;
    }

    // Dipakai saat baca dari file
    public static Category fromCSV(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 2) {
            // Old format without username - migrate to new format with empty username
            return new Category(
                    "",                             // username (empty for migrated data)
                    parts[0],                       // type
                    parts[1]                        // name
            );
        } else if (parts.length == 3) {
            // New format with username
            return new Category(
                    parts[0],                       // username
                    parts[1],                       // type
                    parts[2]                        // name
            );
        } else {
            throw new IllegalArgumentException("Format data tidak valid: " + line);
        }
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }
}
