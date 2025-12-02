# Implementation Plan

- [x] 1. Create Account model and persistence layer






- [x] 1.1 Create Account model class with all required fields

  - Implement Account class in model/ directory
  - Add constants for account types (TYPE_BANK, TYPE_WALLET, TYPE_CASH, TYPE_CREDIT)
  - Implement toCSV() and fromCSV() methods for file persistence
  - Add getters and setters for all fields
  - _Requirements: 1.1, 1.2, 1.5, 7.1_

- [x] 1.2 Create AccountFileManager for file I/O


  - Implement AccountFileManager in persistence/ directory
  - Add saveAccounts() method to write accounts to data/accounts.txt
  - Add loadAccounts() method to read accounts from file
  - Use synchronized methods for thread safety
  - Handle file not found gracefully (return empty list)
  - _Requirements: 1.1, 1.2, 9.1, 9.2_

- [ ]* 1.3 Write property test for Account persistence
  - **Property 1: Account creation persistence**
  - **Validates: Requirements 1.1, 1.2**

- [x] 2. Create Category model and persistence layer



- [x] 2.1 Create Category model class


  - Implement Category class in model/ directory
  - Add constants for category types (TYPE_INCOME, TYPE_EXPENSE)
  - Implement toCSV() and fromCSV() methods
  - Add getters and setters
  - _Requirements: 2.1, 2.2, 2.4, 7.2_

- [x] 2.2 Create CategoryFileManager for file I/O


  - Implement CategoryFileManager in persistence/ directory
  - Add saveCategories() and loadCategories() methods
  - Use synchronized methods for thread safety
  - Seed with default categories on first load
  - _Requirements: 2.1, 2.2, 9.1, 9.2_

- [ ]* 2.3 Write property test for Category persistence
  - **Property 5: Category creation and retrieval**
  - **Validates: Requirements 2.1, 2.2**

- [ ]* 2.4 Write property test for Category type separation
  - **Property 6: Category type separation**
  - **Validates: Requirements 2.4**

- [x] 3. Update Transaction model




- [x] 3.1 Update Transaction class with new fields


  - Add accountName field (String)
  - Add accountType field (String)
  - Change amount field from double to long
  - Update toCSV() to include new fields
  - Update fromCSV() to parse new fields
  - Update all getters and setters
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 7.3_

- [ ]* 3.2 Write property test for Transaction account information persistence
  - **Property 7: Transaction account information persistence**
  - **Validates: Requirements 3.1, 3.2, 3.3, 3.4**

- [ ]* 3.3 Write property test for amount type consistency
  - **Property 8: Amount type consistency**
  - **Validates: Requirements 4.1, 4.2, 4.3, 4.4**
-

- [x] 4. Create AccountService


- [x] 4.1 Implement AccountService class


  - Create AccountService in service/ directory
  - Implement addAccount() method with UUID generation
  - Implement getAllAccounts() method
  - Implement updateAccount() method
  - Implement deleteAccount() method
  - Implement getAccountById() method
  - Use synchronized methods for thread safety
  - Load accounts from file on initialization
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 9.1_

- [ ]* 4.2 Write property test for Account update persistence
  - **Property 2: Account update persistence**
  - **Validates: Requirements 1.3**

- [ ]* 4.3 Write property test for Account deletion completeness
  - **Property 3: Account deletion completeness**
  - **Validates: Requirements 1.4**

- [ ]* 4.4 Write property test for Account balance type consistency
  - **Property 4: Account balance type consistency**
  - **Validates: Requirements 1.5**





- [x] 5. Create CategoryService







- [x] 5.1 Implement CategoryService class

  - Create CategoryService in service/ directory
  - Implement addCategory() method
  - Implement getAllCategories() method
  - Implement deleteCategory() method
  - Implement getCategoriesByType() method
  - Use synchronized methods for thread safety
  - Load categories from file on initialization
  - _Requirements: 2.1, 2.2, 2.3, 9.1_

- [x] 5.2 Create Session model and SessionManager




- [x] 5.2.1 Create Session model class

  - Implement Session class in model/ directory
  - Add fields: sessionToken, username, createdAt, lastAccessedAt
  - Add SESSION_TIMEOUT constant (30 minutes)
  - Implement isExpired() method to check if session has timed out
  - Add getters and setters for all fields
  - _Requirements: 11.1, 11.5_


- [x] 5.2.2 Implement SessionManager class

  - Create SessionManager in service/ directory
  - Use ConcurrentHashMap for thread-safe session storage
  - Implement createSession() method with UUID generation
  - Implement validateSession() method to check token validity and expiration
  - Implement getUsernameFromSession() method
  - Implement updateLastAccessed() method
  - Implement removeSession() method for logout
  - Implement cleanupExpiredSessions() method
  - Implement getActiveSessions() method for multi-device support
  - Use synchronized methods where needed
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7_


- [x] 5.2.3 Implement background cleanup thread


  - Create scheduled task to run cleanupExpiredSessions() every 5 minutes
  - Start cleanup thread when SessionManager is initialized
  - Ensure thread is daemon thread so it doesn't prevent JVM shutdown
  - _Requirements: 11.5_

- [ ]* 5.2.4 Write property test for session creation
  - **Property 13: Session creation on successful login**
  - **Validates: Requirements 11.1**

- [ ]* 5.2.5 Write property test for session validation
  - **Property 14: Session validation for authenticated commands**
  - **Validates: Requirements 11.2**

- [ ]* 5.2.6 Write property test for invalid session error handling
  - **Property 15: Invalid session error handling**
  - **Validates: Requirements 11.3**

- [ ]* 5.2.7 Write property test for session removal on logout
  - **Property 16: Session removal on logout**
  - **Validates: Requirements 11.4**

- [ ]* 5.2.8 Write property test for session timeout
  - **Property 17: Session timeout behavior**
  - **Validates: Requirements 11.5**

- [ ]* 5.2.9 Write property test for multi-device support
  - **Property 18: Multi-device session support**
  - **Validates: Requirements 11.7**

- [x] 6. Update TransactionService



- [x] 6.1 Update existing methods to handle new Transaction fields

  - Update addTransactionFromCommand() to parse accountName and accountType
  - Update updateTransactionFromCommand() to handle new fields
  - Update buildAllTransactionsMessage() to include new fields in response
  - Change amount parsing from double to long
  - _Requirements: 3.1, 3.2, 4.1, 4.3_

- [x] 6.2 Implement getMonthlySummary() method


  - Parse yearMonth parameter (format: YYYY-MM)
  - Filter transactions by username and yearMonth
  - Calculate total income (sum of INCOME transactions)
  - Calculate total expense (sum of EXPENSE transactions)
  - Calculate balance (income - expense)
  - Return formatted response: MONTHLY_SUMMARY|yearMonth|income|expense|balance
  - _Requirements: 5.1, 5.2, 5.3_

- [ ]* 6.3 Write property test for monthly summary calculation
  - **Property 9: Monthly summary calculation correctness**
  - **Validates: Requirements 5.1, 5.2, 5.3**

- [x] 6.4 Implement getCategoryBreakdown() method

  - Parse yearMonth parameter
  - Filter transactions by username, yearMonth, and type=EXPENSE
  - Group by category and sum amounts
  - Return formatted response: CATEGORY_BREAKDOWN|count|cat1|amount1|cat2|amount2|...
  - _Requirements: 6.1, 6.2, 6.3_

- [ ]* 6.5 Write property test for category breakdown calculation
  - **Property 10: Category breakdown calculation correctness**
  - **Validates: Requirements 6.1, 6.2, 6.3**

- [x] 6.6 Implement filterTransactions() method

  - Accept optional parameters: date, category, account
  - Filter transactions based on non-empty parameters
  - Return filtered list in same format as GET_ALL
  - _Requirements: 8.1, 8.2, 8.3_

- [ ]* 6.7 Write property test for transaction filter correctness
  - **Property 11: Transaction filter correctness**
  - **Validates: Requirements 8.1, 8.2, 8.3**
-

- [x] 7. Update Backend2Facade with new commands



- [x] 7.1 Add Account command handlers


  - Implement ADD_ACCOUNT command handler
  - Implement GET_ACCOUNTS command handler
  - Implement UPDATE_ACCOUNT command handler
  - Implement DELETE_ACCOUNT command handler
  - Return proper error responses for invalid inputs
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 10.1, 10.2, 10.4_

- [x] 7.2 Add Category command handlers


  - Implement ADD_CATEGORY command handler
  - Implement GET_CATEGORIES command handler
  - Implement DELETE_CATEGORY command handler
  - Return proper error responses for invalid inputs
  - _Requirements: 2.1, 2.2, 2.3, 10.1, 10.2, 10.4_

- [x] 7.3 Add new Transaction command handlers


  - Implement GET_MONTHLY_SUMMARY command handler
  - Implement GET_CATEGORY_BREAKDOWN command handler
  - Implement FILTER_TRANSACTIONS command handler
  - Return proper error responses for invalid inputs
  - _Requirements: 5.1, 6.1, 8.1, 10.1, 10.2, 10.4_

- [x] 7.4 Update existing Transaction command handlers


  - Update ADD command handler to accept accountName and accountType
  - Update UPDATE command handler to accept new fields
  - Maintain backward compatibility with old command format
  - _Requirements: 3.1, 4.1_

- [ ]* 7.5 Write property test for error response format
  - **Property 12: Error response format consistency**
  - **Validates: Requirements 10.1, 10.2, 10.4**
-

- [ ] 7.6 Add Session command handlers and validation







- [x] 7.6.1 Update LOGIN command handler




  - Modify existing LOGIN command to create session via SessionManager
  - Return session token in response: OK|sessionToken
  - _Requirements: 11.1_

- [x] 7.6.2 Add LOGOUT command handler




  - Implement LOGOUT command handler
  - Parse sessionToken from command
  - Call SessionManager.removeSession()
  - Return OK response
  - _Requirements: 11.4_

- [x] 7.6.3 Add VALIDATE_SESSION command handler




  - Implement VALIDATE_SESSION command handler
  - Parse sessionToken from command
  - Validate via SessionManager
  - Return OK|username or ERROR|SESSION_INVALID
  - _Requirements: 11.2, 11.3_

- [x] 7.6.4 Add session validation to all authenticated commands




  - Update all command handlers (except LOGIN, REGISTER) to validate session token
  - Extract sessionToken as first parameter after command name
  - Call SessionManager.validateSession() before processing command
  - Return ERROR|SESSION_REQUIRED if token missing
  - Return ERROR|SESSION_INVALID if token invalid or expired
  - Update SessionManager.updateLastAccessed() on successful validation
  - _Requirements: 11.2, 11.3_

- [x] 7.6.5 Update command format documentation





  - Update all command examples in code comments to include sessionToken
  - Document breaking change: all authenticated commands now require session token
  - _Requirements: 11.2_

- [x] 8. Create data migration utility







- [x] 8.1 Implement data migration script

  - Read existing database_transactions.txt
  - Convert amount from double to long (if needed)
  - Add default accountName = "-" and accountType = "-"


  - Write to new format
  - Create backup of original file


  - _Requirements: 3.3, 4.2_
-

- [x] 9. Update FinancialSummary model




- [x] 9.1 Update FinancialSummary to use long instead of double


  - Change totalIncome, totalExpense, balance fields to long
  - Update calculation methods
  - Update toProtocolString() method
  - _Requirements: 4.4_

- [x] 10. Checkpoint - Ensure all tests pass




  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Integration testing and validation
- [ ] 11.1 Test complete command flow
  - Test account CRUD operations end-to-end
  - Test category CRUD operations end-to-end
  - Test transaction operations with account info
  - Test summary and breakdown commands
  - Test filter command with various criteria
  - Verify file persistence works correctly
  - _Requirements: All_

- [ ]* 11.2 Write integration tests for multi-operation sequences
  - Test adding account, then adding transaction with that account
  - Test adding categories, then filtering transactions by category
  - Test monthly summary with transactions across multiple months
  - _Requirements: All_

- [ ] 12. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
