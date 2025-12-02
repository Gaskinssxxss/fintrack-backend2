package util;

import model.Transaction;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility for migrating transaction data from old format to new format.
 * 
 * Old format: id|username|date|description|category|type|amount
 * New format: id|username|date|description|category|type|amount|accountName|accountType
 * 
 * Changes:
 * - Adds accountName and accountType fields (default to "-" if missing)
 * - Ensures amount is stored as long integer
 */
public class DataMigrationUtil {
    
    private static final String TRANSACTIONS_FILE = "data/database_transactions.txt";
    private static final String DEFAULT_ACCOUNT_NAME = "-";
    private static final String DEFAULT_ACCOUNT_TYPE = "-";
    
    public static void main(String[] args) {
        System.out.println("=== FinTrack Data Migration Utility ===");
        System.out.println("This utility will migrate your transaction data to the new format.");
        System.out.println();
        
        try {
            DataMigrationUtil migrator = new DataMigrationUtil();
            migrator.migrateTransactions();
            System.out.println("\nMigration completed successfully!");
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Migrates transaction data from old format to new format.
     * Creates a backup before migration.
     */
    public void migrateTransactions() throws IOException {
        migrateTransactions(TRANSACTIONS_FILE);
    }
    
    /**
     * Migrates transaction data from old format to new format.
     * Creates a backup before migration.
     * 
     * @param filePath Path to the transactions file
     */
    public void migrateTransactions(String filePath) throws IOException {
        File transactionsFile = new File(filePath);
        
        // Check if file exists
        if (!transactionsFile.exists()) {
            System.out.println("No transactions file found at: " + TRANSACTIONS_FILE);
            System.out.println("Nothing to migrate.");
            return;
        }
        
        // Create backup
        String backupPath = createBackup(transactionsFile);
        System.out.println("Backup created: " + backupPath);
        
        // Read all transactions
        List<String> lines = Files.readAllLines(transactionsFile.toPath());
        System.out.println("Read " + lines.size() + " transactions");
        
        // Analyze and migrate
        int oldFormatCount = 0;
        int newFormatCount = 0;
        int convertedCount = 0;
        List<String> migratedLines = new ArrayList<>();
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            String[] parts = line.split("\\|", -1);
            
            // Old format has 7 fields, new format has 9 fields
            if (parts.length == 7) {
                // Old format - needs migration
                oldFormatCount++;
                String migratedLine = migrateOldFormat(parts);
                migratedLines.add(migratedLine);
                convertedCount++;
            } else if (parts.length == 9) {
                // New format - keep as is
                newFormatCount++;
                migratedLines.add(line);
            } else {
                // Invalid format - keep as is and warn
                System.out.println("Warning: Invalid format (expected 7 or 9 fields, got " + parts.length + "): " + line);
                migratedLines.add(line);
            }
        }
        
        // Write migrated data
        Files.write(transactionsFile.toPath(), migratedLines);
        
        // Print summary
        System.out.println("\nMigration Summary:");
        System.out.println("- Old format transactions: " + oldFormatCount);
        System.out.println("- New format transactions: " + newFormatCount);
        System.out.println("- Converted transactions: " + convertedCount);
        System.out.println("- Total transactions: " + migratedLines.size());
    }
    
    /**
     * Migrates a transaction from old format to new format.
     * 
     * @param parts Array of 7 fields from old format
     * @return Migrated line in new format
     */
    private String migrateOldFormat(String[] parts) {
        // Old format: id|username|date|description|category|type|amount
        String id = parts[0];
        String username = parts[1];
        String date = parts[2];
        String description = parts[3];
        String category = parts[4];
        String type = parts[5];
        String amountStr = parts[6];
        
        // Convert amount from double to long if needed
        long amount = convertAmount(amountStr);
        
        // Add default account fields
        String accountName = DEFAULT_ACCOUNT_NAME;
        String accountType = DEFAULT_ACCOUNT_TYPE;
        
        // New format: id|username|date|description|category|type|amount|accountName|accountType
        return String.join("|", 
            id, username, date, description, category, type, 
            String.valueOf(amount), accountName, accountType
        );
    }
    
    /**
     * Converts amount string to long integer.
     * Handles both double and long formats.
     * 
     * @param amountStr Amount as string
     * @return Amount as long integer
     */
    private long convertAmount(String amountStr) {
        try {
            // Try parsing as double first (old format)
            if (amountStr.contains(".")) {
                double doubleAmount = Double.parseDouble(amountStr);
                // Convert to long (assuming already in smallest unit)
                return (long) doubleAmount;
            } else {
                // Already a long integer
                return Long.parseLong(amountStr);
            }
        } catch (NumberFormatException e) {
            System.err.println("Warning: Invalid amount format: " + amountStr + ", defaulting to 0");
            return 0L;
        }
    }
    
    /**
     * Creates a backup of the transactions file.
     * 
     * @param originalFile Original transactions file
     * @return Path to backup file
     * @throws IOException If backup creation fails
     */
    private String createBackup(File originalFile) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = "database_transactions_backup_" + timestamp + ".txt";
        File backupFile = new File(originalFile.getParent(), backupFileName);
        
        Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        return backupFile.getAbsolutePath();
    }
    
    /**
     * Validates that all transactions in the file are in the new format.
     * 
     * @return true if all transactions are valid, false otherwise
     */
    public boolean validateMigration() throws IOException {
        return validateMigration(TRANSACTIONS_FILE);
    }
    
    /**
     * Validates that all transactions in the file are in the new format.
     * 
     * @param filePath Path to the transactions file
     * @return true if all transactions are valid, false otherwise
     */
    public boolean validateMigration(String filePath) throws IOException {
        File transactionsFile = new File(filePath);
        
        if (!transactionsFile.exists()) {
            return true; // No file means nothing to validate
        }
        
        List<String> lines = Files.readAllLines(transactionsFile.toPath());
        boolean allValid = true;
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            String[] parts = line.split("\\|", -1);
            
            if (parts.length != 9) {
                System.out.println("Invalid transaction format: " + line);
                allValid = false;
            }
        }
        
        return allValid;
    }
}
