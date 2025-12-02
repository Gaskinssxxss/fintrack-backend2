package util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestLogger {
    private static final String LOG_DIR = "../logs";
    private static final String LOG_FILE = "../logs/request_log.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static RequestLogger instance;
    
    private RequestLogger() {
        ensureLogDirectoryExists();
    }
    
    public static synchronized RequestLogger getInstance() {
        if (instance == null) {
            instance = new RequestLogger();
        }
        return instance;
    }
    
    private void ensureLogDirectoryExists() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            System.out.println("[RequestLogger] Log directory created: " + created + " at " + logDir.getAbsolutePath());
        }
    }
    
    public synchronized void log(String command, String status, String details) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            String logEntry = String.format("[%s] COMMAND: %s | STATUS: %s | DETAILS: %s",
                timestamp, command, status, details);
            
            out.println(logEntry);
            out.flush();
            
        } catch (IOException e) {
            System.err.println("[RequestLogger] Failed to write to log file: " + e.getMessage());
            System.err.println("[RequestLogger] Log file path: " + new File(LOG_FILE).getAbsolutePath());
            e.printStackTrace();
        }
    }
    
    public synchronized void logRequest(String command, String username) {
        log(command, "RECEIVED", "User: " + username);
    }
    
    public synchronized void logSuccess(String command, String message) {
        log(command, "SUCCESS", message);
    }
    
    public synchronized void logError(String command, String error) {
        log(command, "ERROR", error);
    }
    
    public synchronized void logInfo(String command, String info) {
        log(command, "INFO", info);
    }
}
