package test;

import util.RequestLogger;

public class TestRequestLogger {
    public static void main(String[] args) {
        System.out.println("=== Testing RequestLogger ===\n");
        
        RequestLogger logger = RequestLogger.getInstance();
        
        System.out.println("Test 1: Log a request");
        logger.logRequest("LOGIN|testuser", "testuser");
        
        System.out.println("Test 2: Log success");
        logger.logSuccess("LOGIN", "User logged in successfully");
        
        System.out.println("Test 3: Log error");
        logger.logError("ADD", "Invalid parameters");
        
        System.out.println("Test 4: Log info");
        logger.logInfo("BROADCAST", "Data updated");
        
        System.out.println("\n=== Tests completed ===");
        System.out.println("Check file: fintrack-backend2/logs/request_log.txt");
    }
}
