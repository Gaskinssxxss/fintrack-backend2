# Design Document

## Overview

Desain ini akan memperluas Backend FinTrack untuk menyediakan semua API yang dibutuhkan oleh frontend, dengan fokus pada:
1. Menambahkan manajemen Account dan Category
2. Memperbarui model Transaction untuk menyertakan informasi account
3. Mengubah tipe data amount dari double ke long untuk konsistensi
4. Menambahkan API untuk summary dan breakdown data
5. Menambahkan Session Manager untuk autentikasi dan manajemen sesi user
6. Memastikan thread-safety dan error handling yang baik

## Architecture

Backend menggunakan arsitektur 3-layer yang sudah ada dengan tambahan Session Management:
- **Model Layer**: Data models (User, Transaction, Account, Category, Session)
- **Service Layer**: Business logic (UserManager, TransactionService, AccountService, CategoryService, SessionManager)
- **Persistence Layer**: File I/O (FileManager, AccountFileManager, CategoryFileManager)
- **Facade Layer**: API entry point (Backend2Facade)

### Communication Flow
```
Frontend (Socket Client)
    ↓
ClientHandler (Socket Server)
    ↓
Backend2Facade (Command Parser)
    ↓
SessionManager (Validate Token) ← untuk command yang memerlukan autentikasi
    ↓
Service Layer (Business Logic)
    ↓
Persistence Layer (File I/O)
    ↓
Data Files (users.txt, database_transactions.txt, accounts.txt, categories.txt)
```

## Components and Interfaces

### 1. Model Layer

#### Account Model
```java
public class Account {
    private String id;          // UUID
    private String name;        // Nama akun (e.g., "BCA Savings")
    private String number;      // Nomor akun (e.g., "1234567890")
    private long balance;       // Saldo (dalam satuan terkecil, e.g., rupiah)
    private String type;        // TYPE_BANK, TYPE_WALLET, TYPE_CASH, TYPE_CREDIT
    
    // Constants
    public static final String TYPE_BANK = "Bank";
    public static final String TYPE_WALLET = "Dompet Digital";
    public static final String TYPE_CASH = "Cash";
    public static final String TYPE_CREDIT = "Kredit";
}
```

#### Category Model
```java
public class Category {
    private String type;        // "Pemasukan" atau "Pengeluaran"
    private String name;        // Nama kategori (e.g., "Makanan & Minuman")
    
    // Constants
    public static final String TYPE_INCOME = "Pemasukan";
    public static final String TYPE_EXPENSE = "Pengeluaran";
}
```

#### Updated Transaction Model
```java
public class Transaction {
    private String id;
    private String username;
    private String date;
    private String description;
    private String category;
    private String type;
    private long amount;           // Changed from double to long
    private String accountName;    // New field
    private String accountType;    // New field
}
```

#### Session Model
```java
public class Session {
    private String sessionToken;      // UUID untuk session token
    private String username;          // Username yang login
    private long createdAt;           // Timestamp pembuatan session (milliseconds)
    private long lastAccessedAt;      // Timestamp akses terakhir (milliseconds)
    
    // Constants
    public static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 menit dalam milliseconds
}
```

### 2. Service Layer

#### AccountService
```java
public class AccountService {
    private final AccountFileManager fileManager;
    
    public synchronized String addAccount(String name, String number, long balance, String type);
    public synchronized List<Account> getAllAccounts();
    public synchronized boolean updateAccount(String id, String name, String number, long balance, String type);
    public synchronized boolean deleteAccount(String id);
    public synchronized Account getAccountById(String id);
}
```

#### CategoryService
```java
public class CategoryService {
    private final CategoryFileManager fileManager;
    
    public synchronized void addCategory(String type, String name);
    public synchronized List<Category> getAllCategories();
    public synchronized boolean deleteCategory(String type, String name);
    public synchronized List<Category> getCategoriesByType(String type);
}
```

#### Updated TransactionService
```java
public class TransactionService {
    // Existing methods updated to handle accountName and accountType
    public synchronized void addTransaction(String username, String date, String description, 
                                           String category, String type, long amount,
                                           String accountName, String accountType);
    
    // New methods
    public synchronized Map<String, Long> getMonthlySummary(String username, String yearMonth);
    public synchronized Map<String, Long> getCategoryBreakdown(String username, String yearMonth);
    public synchronized List<Transaction> filterTransactions(String date, String category, String account);
}
```

#### SessionManager
```java
public class SessionManager {
    private final Map<String, Session> sessions;  // Thread-safe ConcurrentHashMap
    
    public synchronized String createSession(String username);
    public synchronized boolean validateSession(String sessionToken);
    public synchronized String getUsernameFromSession(String sessionToken);
    public synchronized void updateLastAccessed(String sessionToken);
    public synchronized void removeSession(String sessionToken);
    public synchronized void cleanupExpiredSessions();
    public synchronized List<Session> getActiveSessions(String username);
}
```

### 3. Persistence Layer

#### AccountFileManager
```java
public class AccountFileManager {
    private static final String ACCOUNTS_FILE = "data/accounts.txt";
    
    public synchronized void saveAccounts(List<Account> accounts);
    public synchronized List<Account> loadAccounts();
}
```

#### CategoryFileManager
```java
public class CategoryFileManager {
    private static final String CATEGORIES_FILE = "data/categories.txt";
    
    public synchronized void saveCategories(List<Category> categories);
    public synchronized List<Category> loadCategories();
}
```

### 4. Facade Layer - Command Protocol

#### Account Commands
- `ADD_ACCOUNT|name|number|balance|type` → `OK|accountId`
- `GET_ACCOUNTS` → `DATA_ACCOUNTS|count|id1|name1|number1|balance1|type1|id2|...`
- `UPDATE_ACCOUNT|id|name|number|balance|type` → `OK`
- `DELETE_ACCOUNT|id` → `OK`

#### Category Commands
- `ADD_CATEGORY|type|name` → `OK`
- `GET_CATEGORIES` → `DATA_CATEGORIES|count|type1|name1|type2|name2|...`
- `DELETE_CATEGORY|type|name` → `OK`

#### Updated Transaction Commands
- `ADD|username|date|description|category|type|amount|accountName|accountType` → `SUMMARY|...`
- `UPDATE|id|username|date|description|category|type|amount|accountName|accountType` → `SUMMARY|...`
- `GET_ALL` → `DATA_ALL|count|id1|username1|date1|desc1|cat1|type1|amount1|accName1|accType1|...`

#### New Summary Commands
- `GET_MONTHLY_SUMMARY|username|yearMonth` → `MONTHLY_SUMMARY|yearMonth|income|expense|balance`
- `GET_CATEGORY_BREAKDOWN|username|yearMonth` → `CATEGORY_BREAKDOWN|count|cat1|amount1|cat2|amount2|...`
- `FILTER_TRANSACTIONS|date|category|account` → `DATA_ALL|...`

#### Session Commands
- `LOGIN|username|password` → `OK|sessionToken` (existing command, updated to return session token)
- `LOGOUT|sessionToken` → `OK`
- `VALIDATE_SESSION|sessionToken` → `OK|username` atau `ERROR|SESSION_INVALID`

**Note**: Semua command yang memerlukan autentikasi (kecuali LOGIN dan REGISTER) harus menyertakan sessionToken sebagai parameter pertama setelah command name.

Format baru untuk authenticated commands:
- `ADD|sessionToken|date|description|category|type|amount|accountName|accountType` → `SUMMARY|...`
- `GET_ALL|sessionToken` → `DATA_ALL|...`
- `ADD_ACCOUNT|sessionToken|name|number|balance|type` → `OK|accountId`
- dst.

## Data Models

### File Formats

#### accounts.txt
```
id|name|number|balance|type
uuid1|BCA Savings|1234567890|5000000|Bank
uuid2|GoPay|081234567890|250000|Dompet Digital
```

#### categories.txt
```
type|name
Pengeluaran|Makanan & Minuman
Pengeluaran|Transportasi
Pemasukan|Gaji
Pemasukan|Freelance
```

#### database_transactions.txt (updated)
```
id|username|date|description|category|type|amount|accountName|accountType
uuid1|john|2025-11-18|Lunch|Makanan & Minuman|EXPENSE|50000|BCA Savings|Bank
uuid2|john|2025-11-18|Salary|Gaji|INCOME|5000000|BCA Savings|Bank
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Property 1: Account creation persistence
*For any* valid account data (name, number, balance, type), when ADD_ACCOUNT command is sent, the backend should return OK with a valid UUID and the account should be retrievable via GET_ACCOUNTS
**Validates: Requirements 1.1, 1.2**

Property 2: Account update persistence
*For any* existing account and any valid update data, when UPDATE_ACCOUNT command is sent, the updated values should persist and be retrievable
**Validates: Requirements 1.3**

Property 3: Account deletion completeness
*For any* existing account, when DELETE_ACCOUNT command is sent, the account should no longer appear in GET_ACCOUNTS results
**Validates: Requirements 1.4**

Property 4: Account balance type consistency
*For any* account with balance value, when saved and reloaded from file, the balance should remain a long integer without precision loss
**Validates: Requirements 1.5**

Property 5: Category creation and retrieval
*For any* valid category (type and name), when ADD_CATEGORY command is sent, the category should appear in GET_CATEGORIES results
**Validates: Requirements 2.1, 2.2**

Property 6: Category type separation
*For any* set of income and expense categories, when retrieved via GET_CATEGORIES, income categories should be distinguishable from expense categories by their type field
**Validates: Requirements 2.4**

Property 7: Transaction account information persistence
*For any* transaction with accountName and accountType, when saved and retrieved, the account information should be preserved exactly
**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

Property 8: Amount type consistency
*For any* transaction amount value, when sent via ADD command, stored to file, and retrieved, the value should remain a long integer without precision loss or rounding errors
**Validates: Requirements 4.1, 4.2, 4.3, 4.4**

Property 9: Monthly summary calculation correctness
*For any* set of transactions in a given month, the monthly summary income should equal the sum of all income transactions, expense should equal the sum of all expense transactions, and balance should equal income minus expense
**Validates: Requirements 5.1, 5.2, 5.3**

Property 10: Category breakdown calculation correctness
*For any* set of transactions in a given month, the category breakdown should only include expense transactions and the sum of all category amounts should equal the total expenses for that month
**Validates: Requirements 6.1, 6.2, 6.3**

Property 11: Transaction filter correctness
*For any* set of transactions and any filter criteria (date, category, account), the filtered results should only include transactions matching all specified non-empty criteria
**Validates: Requirements 8.1, 8.2, 8.3**

Property 12: Error response format consistency
*For any* invalid command or failed operation, the backend should return an error response in the format ERROR|ERROR_CODE|description
**Validates: Requirements 10.1, 10.2, 10.4**

Property 13: Session creation on successful login
*For any* valid username and password combination, when LOGIN command is sent, the backend should return OK with a valid UUID session token that can be used for subsequent authenticated requests
**Validates: Requirements 11.1**

Property 14: Session validation for authenticated commands
*For any* authenticated command with an invalid or missing session token, the backend should reject the command and return an error before processing the command logic
**Validates: Requirements 11.2**

Property 15: Invalid session error handling
*For any* invalid or expired session token, when used in an authenticated command, the backend should return ERROR|SESSION_INVALID
**Validates: Requirements 11.3**

Property 16: Session removal on logout
*For any* valid session token, when LOGOUT command is sent, the session should be removed and subsequent requests with that token should return SESSION_INVALID
**Validates: Requirements 11.4**

Property 17: Session timeout behavior
*For any* session that has not been accessed for longer than SESSION_TIMEOUT period, the session should be automatically removed and return SESSION_INVALID when used
**Validates: Requirements 11.5**

Property 18: Multi-device session support
*For any* username, when multiple LOGIN commands are sent (simulating different devices), all returned session tokens should remain valid and usable independently
**Validates: Requirements 11.7**

## Error Handling

### Error Codes
- `EMPTY_COMMAND`: Command string is null or empty
- `UNKNOWN_COMMAND`: Command not recognized
- `INVALID_FORMAT`: Command format is incorrect
- `ACCOUNT_NOT_FOUND`: Account ID does not exist
- `CATEGORY_NOT_FOUND`: Category does not exist
- `TRANSACTION_NOT_FOUND`: Transaction ID does not exist
- `INVALID_AMOUNT`: Amount cannot be parsed as long
- `INVALID_DATE`: Date format is incorrect
- `INVALID_TYPE`: Type value is not recognized
- `FILE_ERROR`: Error reading or writing to file
- `SESSION_INVALID`: Session token tidak valid atau sudah expired
- `SESSION_REQUIRED`: Command memerlukan session token yang valid
- `UNAUTHORIZED`: User tidak memiliki akses untuk operasi ini

### Error Response Format
All errors follow the pattern: `ERROR|ERROR_CODE|description`

Example:
```
ERROR|ACCOUNT_NOT_FOUND|Account with ID abc123 does not exist
ERROR|INVALID_AMOUNT|Amount must be a positive long integer
```

### Success Response Format
- Simple operations: `OK` or `OK|additionalData`
- Data retrieval: `DATA_TYPE|count|field1|field2|...`
- Summary operations: `SUMMARY|field1|field2|...`

## Testing Strategy

### Unit Testing
Unit tests will cover:
- Model serialization/deserialization (toCSV/fromCSV methods)
- Service layer individual methods with specific examples
- File manager read/write operations
- Command parsing in Backend2Facade
- Edge cases like empty strings, null values, boundary values

### Property-Based Testing
Property-based tests will verify universal properties using a Java PBT library (jqwik or QuickTheories). Each test will run a minimum of 100 iterations with randomly generated inputs.

Property-based tests will cover:
- Account CRUD operations (Properties 1-4)
- Category CRUD operations (Properties 5-6)
- Transaction persistence with account info (Property 7)
- Amount type consistency (Property 8)
- Summary calculations (Properties 9-10)
- Transaction filtering (Property 11)
- Error handling (Property 12)
- Session management (Properties 13-18)

Each property-based test will be tagged with a comment referencing the design document:
```java
// Feature: backend-frontend-alignment, Property 1: Account creation persistence
// Validates: Requirements 1.1, 1.2
@Property
void accountCreationPersistence(@ForAll Account account) {
    // test implementation
}
```

### Integration Testing
Integration tests will verify:
- End-to-end command flow from socket to file and back
- Multiple operations in sequence (add, update, delete)
- Data consistency across service boundaries
- File persistence and reload

### Thread Safety Testing
While difficult to test with PBT, thread safety will be verified through:
- Code review of synchronized methods
- Manual concurrent testing with multiple clients
- Stress testing with high load

## Implementation Notes

### Migration Strategy
1. Create new model classes (Account, Category, Session)
2. Update Transaction model (add accountName, accountType, change amount to long)
3. Create new service classes (AccountService, CategoryService, SessionManager)
4. Update TransactionService to handle new fields
5. Create new file managers (AccountFileManager, CategoryFileManager)
6. Update Backend2Facade to handle new commands and session validation
7. Update UserManager to integrate with SessionManager for login/logout
8. Migrate existing transaction data (convert double to long, add default account values)
9. Update all existing commands to support session token parameter
10. Update tests

### Data Migration
Existing transactions in `database_transactions.txt` need to be migrated:
- Convert amount from double to long (multiply by 1 if already in smallest unit)
- Add default accountName = "-" and accountType = "-" for existing transactions
- Backup original file before migration

### Backward Compatibility
- Old ADD command format (without account info) should still work with default values
- Old GET_ALL response format should be extended, not replaced
- Commands without session token will be rejected with SESSION_REQUIRED error (breaking change for security)
- Frontend must be updated to include session token in all authenticated commands

### Session Management Implementation Details
- Use ConcurrentHashMap for thread-safe session storage
- Implement background thread for periodic cleanup of expired sessions (runs every 5 minutes)
- Session timeout set to 30 minutes of inactivity
- Session token generated using UUID.randomUUID()
- lastAccessedAt updated on every successful command execution
- Multiple sessions per user allowed for multi-device support
