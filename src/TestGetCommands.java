/**
 * Test untuk memverifikasi GET commands bekerja dengan benar
 * dengan session token dan filtering by username
 */
public class TestGetCommands {
    
    public static void main(String[] args) {
        Backend2Facade facade = new Backend2Facade();
        
        System.out.println("=== Test GET Commands ===\n");
        
        // 1. Register 2 users
        System.out.println("1. Register users:");
        String reg1 = facade.handleTextCommand("REGISTER|user1|pass1");
        String reg2 = facade.handleTextCommand("REGISTER|user2|pass2");
        System.out.println("   User1: " + reg1);
        System.out.println("   User2: " + reg2);
        
        // 2. Login both users
        System.out.println("\n2. Login users:");
        String login1 = facade.handleTextCommand("LOGIN|user1|pass1");
        String login2 = facade.handleTextCommand("LOGIN|user2|pass2");
        System.out.println("   User1: " + login1);
        System.out.println("   User2: " + login2);
        
        // Extract session tokens
        String token1 = login1.split("\\|")[1];
        String token2 = login2.split("\\|")[1];
        
        // 3. Add transactions for user1
        System.out.println("\n3. Add transactions for user1:");
        String add1 = facade.handleTextCommand("ADD|" + token1 + "|2024-01-01|Gaji|Salary|INCOME|5000000|BCA|Bank");
        String add2 = facade.handleTextCommand("ADD|" + token1 + "|2024-01-02|Makan|Food|EXPENSE|50000|Cash|Cash");
        System.out.println("   Add1: " + add1.substring(0, Math.min(50, add1.length())) + "...");
        System.out.println("   Add2: " + add2.substring(0, Math.min(50, add2.length())) + "...");
        
        // 4. Add transactions for user2
        System.out.println("\n4. Add transactions for user2:");
        String add3 = facade.handleTextCommand("ADD|" + token2 + "|2024-01-01|Freelance|Work|INCOME|3000000|Mandiri|Bank");
        String add4 = facade.handleTextCommand("ADD|" + token2 + "|2024-01-03|Transport|Transport|EXPENSE|100000|GoPay|Ewallet");
        System.out.println("   Add3: " + add3.substring(0, Math.min(50, add3.length())) + "...");
        System.out.println("   Add4: " + add4.substring(0, Math.min(50, add4.length())) + "...");
        
        // 5. Test GET_ALL for user1 (should only see user1's transactions)
        System.out.println("\n5. GET_ALL for user1:");
        String getAll1 = facade.handleTextCommand("GET_ALL|" + token1);
        System.out.println("   Response: " + getAll1);
        String[] parts1 = getAll1.split("\\|");
        int count1 = Integer.parseInt(parts1[1]);
        System.out.println("   Transaction count: " + count1 + " (expected: 2)");
        
        // Verify only user1's transactions
        boolean onlyUser1 = true;
        for (int i = 2; i < parts1.length; i += 9) {
            if (i + 1 < parts1.length) {
                String username = parts1[i + 1];
                if (!username.equals("user1")) {
                    onlyUser1 = false;
                    System.out.println("   ERROR: Found transaction for " + username);
                }
            }
        }
        System.out.println("   Only user1 transactions: " + (onlyUser1 ? "PASS" : "FAIL"));
        
        // 6. Test GET_ALL for user2 (should only see user2's transactions)
        System.out.println("\n6. GET_ALL for user2:");
        String getAll2 = facade.handleTextCommand("GET_ALL|" + token2);
        System.out.println("   Response: " + getAll2);
        String[] parts2 = getAll2.split("\\|");
        int count2 = Integer.parseInt(parts2[1]);
        System.out.println("   Transaction count: " + count2 + " (expected: 2)");
        
        // Verify only user2's transactions
        boolean onlyUser2 = true;
        for (int i = 2; i < parts2.length; i += 9) {
            if (i + 1 < parts2.length) {
                String username = parts2[i + 1];
                if (!username.equals("user2")) {
                    onlyUser2 = false;
                    System.out.println("   ERROR: Found transaction for " + username);
                }
            }
        }
        System.out.println("   Only user2 transactions: " + (onlyUser2 ? "PASS" : "FAIL"));
        
        // 7. Test GET_SUMMARY for user1
        System.out.println("\n7. GET_SUMMARY for user1:");
        String summary1 = facade.handleTextCommand("GET_SUMMARY|" + token1);
        System.out.println("   Response: " + summary1);
        String[] summaryParts1 = summary1.split("\\|");
        // Format: SUMMARY|balance|income|expense
        long balance1 = Long.parseLong(summaryParts1[1]);
        long income1 = Long.parseLong(summaryParts1[2]);
        long expense1 = Long.parseLong(summaryParts1[3]);
        System.out.println("   Balance: " + balance1 + " (expected: 4950000)");
        System.out.println("   Income: " + income1 + " (expected: 5000000)");
        System.out.println("   Expense: " + expense1 + " (expected: 50000)");
        boolean summary1Pass = (income1 == 5000000 && expense1 == 50000 && balance1 == 4950000);
        System.out.println("   Summary1: " + (summary1Pass ? "PASS" : "FAIL"));
        
        // 8. Test GET_SUMMARY for user2
        System.out.println("\n8. GET_SUMMARY for user2:");
        String summary2 = facade.handleTextCommand("GET_SUMMARY|" + token2);
        System.out.println("   Response: " + summary2);
        String[] summaryParts2 = summary2.split("\\|");
        // Format: SUMMARY|balance|income|expense
        long balance2 = Long.parseLong(summaryParts2[1]);
        long income2 = Long.parseLong(summaryParts2[2]);
        long expense2 = Long.parseLong(summaryParts2[3]);
        System.out.println("   Balance: " + balance2 + " (expected: 2900000)");
        System.out.println("   Income: " + income2 + " (expected: 3000000)");
        System.out.println("   Expense: " + expense2 + " (expected: 100000)");
        boolean summary2Pass = (income2 == 3000000 && expense2 == 100000 && balance2 == 2900000);
        System.out.println("   Summary2: " + (summary2Pass ? "PASS" : "FAIL"));
        
        // 9. Test invalid session token
        System.out.println("\n9. Test invalid session token:");
        String invalidGet = facade.handleTextCommand("GET_ALL|invalid_token_123");
        System.out.println("   Response: " + invalidGet);
        boolean invalidPass = invalidGet.startsWith("ERROR|SESSION_INVALID");
        System.out.println("   Invalid token handling: " + (invalidPass ? "PASS" : "FAIL"));
        
        // 10. Test missing session token
        System.out.println("\n10. Test missing session token:");
        String missingGet = facade.handleTextCommand("GET_ALL");
        System.out.println("   Response: " + missingGet);
        boolean missingPass = missingGet.startsWith("ERROR|SESSION_REQUIRED");
        System.out.println("   Missing token handling: " + (missingPass ? "PASS" : "FAIL"));
        
        // Summary
        System.out.println("\n=== Test Summary ===");
        System.out.println("GET_ALL user1 filtering: " + (onlyUser1 && count1 == 2 ? "PASS" : "FAIL"));
        System.out.println("GET_ALL user2 filtering: " + (onlyUser2 && count2 == 2 ? "PASS" : "FAIL"));
        System.out.println("GET_SUMMARY user1: " + (summary1Pass ? "PASS" : "FAIL"));
        System.out.println("GET_SUMMARY user2: " + (summary2Pass ? "PASS" : "FAIL"));
        System.out.println("Invalid token handling: " + (invalidPass ? "PASS" : "FAIL"));
        System.out.println("Missing token handling: " + (missingPass ? "PASS" : "FAIL"));
        
        boolean allPass = onlyUser1 && onlyUser2 && count1 == 2 && count2 == 2 
                         && summary1Pass && summary2Pass && invalidPass && missingPass;
        System.out.println("\nOverall: " + (allPass ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));
    }
}
