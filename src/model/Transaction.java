import java.io.Serializable;

public class Transaction implements Serializable {

    private String id;          // ID unik (UUID / timestamp)
    private String username;    // siapa yang input
    private String date;        // format: YYYY-MM-DD
    private String description; // keterangan
    private String category;    // contoh: Gaji, Makanan, Transport
    private String type;        // "INCOME" atau "EXPENSE"
    private double amount;      // nominal

    public Transaction(String id, String username, String date,
                       String description, String category,
                       String type, double amount) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.description = description;
        this.category = category;
        this.type = type;
        this.amount = amount;
    }

    // Dipakai saat simpan ke file (Stream)
    public String toCSV() {
        return id + "|" + username + "|" + date + "|" + description + "|" 
               + category + "|" + type + "|" + amount;
    }

    // Dipakai saat baca dari file
    public static Transaction fromCSV(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 7) {
            throw new IllegalArgumentException("Format data tidak valid: " + line);
        }
        return new Transaction(
                parts[0],                       // id
                parts[1],                       // username
                parts[2],                       // date
                parts[3],                       // description
                parts[4],                       // category
                parts[5],                       // type
                Double.parseDouble(parts[6])    // amount
        );
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

    public double getAmount() {
        return amount;
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

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
