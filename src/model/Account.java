package model;

import java.io.Serializable;

public class Account implements Serializable {

    // Constants for account types
    public static final String TYPE_BANK = "Bank";
    public static final String TYPE_WALLET = "Dompet Digital";
    public static final String TYPE_CASH = "Cash";
    public static final String TYPE_CREDIT = "Kredit";

    private String id;          // UUID
    private String username;    // Username pemilik akun
    private String name;        // Nama akun (e.g., "BCA Savings")
    private String number;      // Nomor akun (e.g., "1234567890")
    private long balance;       // Saldo (dalam satuan terkecil, e.g., rupiah)
    private String type;        // TYPE_BANK, TYPE_WALLET, TYPE_CASH, TYPE_CREDIT

    public Account(String id, String username, String name, String number, long balance, String type) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.number = number;
        this.balance = balance;
        this.type = type;
    }

    // Dipakai saat simpan ke file (Stream)
    public String toCSV() {
        return id + "|" + username + "|" + name + "|" + number + "|" + balance + "|" + type;
    }

    // Dipakai saat baca dari file
    public static Account fromCSV(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 5) {
            // Old format without username - migrate to new format with empty username
            return new Account(
                    parts[0],                       // id
                    "",                             // username (empty for migrated data)
                    parts[1],                       // name
                    parts[2],                       // number
                    Long.parseLong(parts[3]),       // balance
                    parts[4]                        // type
            );
        } else if (parts.length == 6) {
            // New format with username
            return new Account(
                    parts[0],                       // id
                    parts[1],                       // username
                    parts[2],                       // name
                    parts[3],                       // number
                    Long.parseLong(parts[4]),       // balance
                    parts[5]                        // type
            );
        } else {
            throw new IllegalArgumentException("Format data tidak valid: " + line);
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public long getBalance() {
        return balance;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setType(String type) {
        this.type = type;
    }
}
