package test;

import util.DataMigrationUtil;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Test for DataMigrationUtil to verify migration from old to new format.
 */
public class TestDataMigration {
    
    private static final String TEST_FILE = "data/test_transactions.txt";
    
    public static void main(String[] args) {
        TestDataMigration test = new TestDataMigration();
        
        System.out.println("=== Testing Data Migration Utility ===\n");
        
        try {
            test.testMigrationFromOldFormat();
            test.testMigrationWithMixedFormats();
            test.testMigrationWithDoubleAmounts();
            test.testNoMigrationNeeded();
            
            System.out.println("\n=== All Tests Passed! ===");
        } catch (Exception e) {
            System.err.println("\n=== Test Failed! ===");
            e.printStackTrace();
        }
    }
    
    /**
     * Test migration from old format (7 fields) to new format (9 fields).
     */
    public void testMigrationFromOldFormat() throws IOException {
        System.out.println("Test 1: Migration from old format");
        
        // Create test file with old format
        List<String> oldFormatLines = Arrays.asList(
            "id1|user1|2025-11-18|Lunch|Food|EXPENSE|50000",
            "id2|user1|2025-11-18|Salary|Income|INCOME|5000000"
        );
        
        File testFile = new File(TEST_FILE);
        testFile.getParentFile().mkdirs();
        Files.write(testFile.toPath(), oldFormatLines);
        
        // Run migration
        DataMigrationUtil migrator = new DataMigrationUtil();
        migrator.migrateTransactions(TEST_FILE);
        
        // Verify migration
        List<String> migratedLines = Files.readAllLines(testFile.toPath());
        
        assert migratedLines.size() == 2 : "Expected 2 lines, got " + migratedLines.size();
        
        // Check first line
        String[] parts1 = migratedLines.get(0).split("\\|", -1);
        assert parts1.length == 9 : "Expected 9 fields, got " + parts1.length;
        assert parts1[7].equals("-") : "Expected default accountName '-'";
        assert parts1[8].equals("-") : "Expected default accountType '-'";
        assert parts1[6].equals("50000") : "Expected amount 50000";
        
        // Check second line
        String[] parts2 = migratedLines.get(1).split("\\|", -1);
        assert parts2.length == 9 : "Expected 9 fields, got " + parts2.length;
        assert parts2[7].equals("-") : "Expected default accountName '-'";
        assert parts2[8].equals("-") : "Expected default accountType '-'";
        
        // Cleanup
        testFile.delete();
        
        System.out.println("✓ Old format successfully migrated to new format\n");
    }
    
    /**
     * Test migration with mixed old and new formats.
     */
    public void testMigrationWithMixedFormats() throws IOException {
        System.out.println("Test 2: Migration with mixed formats");
        
        // Create test file with mixed formats
        List<String> mixedLines = Arrays.asList(
            "id1|user1|2025-11-18|Lunch|Food|EXPENSE|50000",  // Old format
            "id2|user1|2025-11-18|Salary|Income|INCOME|5000000|BCA|Bank"  // New format
        );
        
        File testFile = new File(TEST_FILE);
        Files.write(testFile.toPath(), mixedLines);
        
        // Run migration
        DataMigrationUtil migrator = new DataMigrationUtil();
        migrator.migrateTransactions(TEST_FILE);
        
        // Verify migration
        List<String> migratedLines = Files.readAllLines(testFile.toPath());
        
        assert migratedLines.size() == 2 : "Expected 2 lines";
        
        // Both should now be in new format
        for (String line : migratedLines) {
            String[] parts = line.split("\\|", -1);
            assert parts.length == 9 : "Expected 9 fields, got " + parts.length;
        }
        
        // First line should have default account values
        String[] parts1 = migratedLines.get(0).split("\\|", -1);
        assert parts1[7].equals("-") : "Expected default accountName";
        
        // Second line should keep original account values
        String[] parts2 = migratedLines.get(1).split("\\|", -1);
        assert parts2[7].equals("BCA") : "Expected accountName 'BCA'";
        assert parts2[8].equals("Bank") : "Expected accountType 'Bank'";
        
        // Cleanup
        testFile.delete();
        
        System.out.println("✓ Mixed formats handled correctly\n");
    }
    
    /**
     * Test migration with double amounts (old format).
     */
    public void testMigrationWithDoubleAmounts() throws IOException {
        System.out.println("Test 3: Migration with double amounts");
        
        // Create test file with double amounts
        List<String> oldFormatLines = Arrays.asList(
            "id1|user1|2025-11-18|Lunch|Food|EXPENSE|50000.0",
            "id2|user1|2025-11-18|Salary|Income|INCOME|5000000.5"
        );
        
        File testFile = new File(TEST_FILE);
        Files.write(testFile.toPath(), oldFormatLines);
        
        // Run migration
        DataMigrationUtil migrator = new DataMigrationUtil();
        migrator.migrateTransactions(TEST_FILE);
        
        // Verify migration
        List<String> migratedLines = Files.readAllLines(testFile.toPath());
        
        // Check amounts are converted to long
        String[] parts1 = migratedLines.get(0).split("\\|", -1);
        assert parts1[6].equals("50000") : "Expected amount 50000, got " + parts1[6];
        
        String[] parts2 = migratedLines.get(1).split("\\|", -1);
        assert parts2[6].equals("5000000") : "Expected amount 5000000, got " + parts2[6];
        
        // Cleanup
        testFile.delete();
        
        System.out.println("✓ Double amounts converted to long\n");
    }
    
    /**
     * Test when no migration is needed (all already in new format).
     */
    public void testNoMigrationNeeded() throws IOException {
        System.out.println("Test 4: No migration needed");
        
        // Create test file with new format
        List<String> newFormatLines = Arrays.asList(
            "id1|user1|2025-11-18|Lunch|Food|EXPENSE|50000|BCA|Bank",
            "id2|user1|2025-11-18|Salary|Income|INCOME|5000000|GoPay|Wallet"
        );
        
        File testFile = new File(TEST_FILE);
        Files.write(testFile.toPath(), newFormatLines);
        
        // Run migration
        DataMigrationUtil migrator = new DataMigrationUtil();
        migrator.migrateTransactions(TEST_FILE);
        
        // Verify no changes
        List<String> migratedLines = Files.readAllLines(testFile.toPath());
        
        assert migratedLines.size() == 2 : "Expected 2 lines";
        assert migratedLines.get(0).equals(newFormatLines.get(0)) : "Line 1 should be unchanged";
        assert migratedLines.get(1).equals(newFormatLines.get(1)) : "Line 2 should be unchanged";
        
        // Cleanup
        testFile.delete();
        
        System.out.println("✓ No unnecessary changes when already in new format\n");
    }
}
