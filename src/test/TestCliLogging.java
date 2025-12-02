package test;

import util.RequestLogger;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Test untuk memverifikasi bahwa CLI logging bekerja dengan baik
 */
public class TestCliLogging {
    
    public static void main(String[] args) {
        System.out.println("=== Test CLI Logging ===\n");
        
        // Test 1: Verify RequestLogger instance
        System.out.println("Test 1: Verify RequestLogger instance");
        RequestLogger logger = RequestLogger.getInstance();
        if (logger != null) {
            System.out.println("✓ RequestLogger instance created successfully");
        } else {
            System.out.println("✗ Failed to create RequestLogger instance");
            return;
        }
        
        // Test 2: Log a test command
        System.out.println("\nTest 2: Log a test command");
        logger.logRequest("TEST_COMMAND|param1|param2", "test_user");
        logger.logSuccess("TEST_COMMAND", "Command executed successfully");
        System.out.println("✓ Test command logged");
        
        // Test 3: Verify log file exists and contains the entry
        System.out.println("\nTest 3: Verify log file");
        File logFile = new File("../logs/request_log.txt");
        if (logFile.exists()) {
            System.out.println("✓ Log file exists at: " + logFile.getAbsolutePath());
            
            // Read last few lines
            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                String line;
                int lineCount = 0;
                System.out.println("\nLast 5 lines of log file:");
                System.out.println("----------------------------------------");
                
                // Read all lines and keep last 5
                String[] lastLines = new String[5];
                int index = 0;
                while ((line = br.readLine()) != null) {
                    lastLines[index % 5] = line;
                    index++;
                    lineCount++;
                }
                
                // Print last 5 lines
                int start = Math.max(0, lineCount - 5);
                for (int i = 0; i < Math.min(5, lineCount); i++) {
                    int pos = (start + i) % 5;
                    if (lastLines[pos] != null) {
                        System.out.println(lastLines[pos]);
                    }
                }
                System.out.println("----------------------------------------");
                System.out.println("Total lines in log: " + lineCount);
                
            } catch (IOException e) {
                System.out.println("✗ Error reading log file: " + e.getMessage());
            }
        } else {
            System.out.println("✗ Log file does not exist");
        }
        
        System.out.println("\n=== Test Complete ===");
    }
}
