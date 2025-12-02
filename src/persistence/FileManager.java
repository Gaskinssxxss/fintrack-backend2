package persistence;

import model.Transaction;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String FILE_NAME = "data/database_transactions.txt";

    public List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Transaction t = Transaction.fromCSV(line);
                    list.add(t);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void saveTransaction(Transaction t) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME, true), "UTF-8"))) {

            writer.write(t.toCSV());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAll(List<Transaction> transactions) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME, false), "UTF-8"))) {

            for (Transaction t : transactions) {
                writer.write(t.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
