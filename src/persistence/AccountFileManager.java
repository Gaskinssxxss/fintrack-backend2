package persistence;

import model.Account;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AccountFileManager {

    private static final String FILE_NAME = "data/accounts.txt";

    /**
     * Load all accounts from file
     * Returns empty list if file doesn't exist
     */
    public synchronized List<Account> loadAccounts() {
        List<Account> list = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Account account = Account.fromCSV(line);
                    list.add(account);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Save all accounts to file
     * Overwrites existing file
     */
    public synchronized void saveAccounts(List<Account> accounts) {
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME, false), "UTF-8"))) {

            for (Account account : accounts) {
                writer.write(account.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
