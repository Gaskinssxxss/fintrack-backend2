package model;

import java.io.Serializable;

public class Category implements Serializable {

    // Constants for category types
    public static final String TYPE_INCOME = "Pemasukan";
    public static final String TYPE_EXPENSE = "Pengeluaran";

    private String type;        // "Pemasukan" atau "Pengeluaran"
    private String name;        // Nama kategori (e.g., "Makanan & Minuman")

    public Category(String type, String name) {
        this.type = type;
        this.name = name;
    }

    // Dipakai saat simpan ke file (Stream)
    public String toCSV() {
        return type + "|" + name;
    }

    // Dipakai saat baca dari file
    public static Category fromCSV(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format data tidak valid: " + line);
        }
        return new Category(
                parts[0],                       // type
                parts[1]                        // name
        );
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }
}
