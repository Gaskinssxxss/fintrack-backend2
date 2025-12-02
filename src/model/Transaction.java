package model;

import java.io.Serializable;

public class Transaction implements Serializable {

    private String id;          // ID unik (UUID / timestamp)
    private String username;    // siapa yang input
    private String date;        // format: YYYY-MM-DD
    private String description; // keterangan
    private String category;    // contoh: Gaji, Makanan, Transport
    private String type;        // "INCOME" atau "EXPENSE"
    private long amount;        // nominal (changed from double to long)
    private String accountName; // nama akun (e.g., "BCA Savings")
    private String accountType; // tipe akun (e.g., "Bank", "Dompet Digital")

    public Transaction(String id, String username, String date,
                       String description, String category,
                       String type, long amount, String accountName, String accountType) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.description = description;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.accountName = accountName;
        this.accountType = accountType;
    }

    // Dipakai saat simpan ke file (Stream)
    public String toCSV() {
        return id + "|" + username + "|" + date + "|" + description + "|" 
               + category + "|" + type + "|" + amount + "|" + accountName + "|" + accountType;
    }

    // Dipakai saat baca dari file
    public static Transaction fromCSV(String line) {
        String[] parts = line.split("\\|");
        // Support both old format (7 fields) and new format (9 fields)
        if (parts.length == 7) {
            // Old format: convert double to long and use default account values
            return new Transaction(
                    parts[0],                       // id
                    parts[1],                       // username
                    parts[2],                       // date
                    parts[3],                       // description
                    parts[4],                       // category
                    parts[5],                       // type
                    (long) Double.parseDouble(parts[6]), // amount (convert double to long)
                    "-",                            // accountName (default)
                    "-"                             // accountType (default)
            );
        } else if (parts.length == 9) {
            // New format
            return new Transaction(
                    parts[0],                       // id
                    parts[1],                       // username
                    parts[2],                       // date
                    parts[3],                       // description
                    parts[4],                       // category
                    parts[5],                       // type
                    Long.parseLong(parts[6]),       // amount
                    parts[7],                       // accountName
                    parts[8]                        // accountType
            );
        } else {
            throw new IllegalArgumentException("Format data tidak valid: " + line);
        }
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
