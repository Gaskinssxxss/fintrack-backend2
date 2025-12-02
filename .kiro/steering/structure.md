# Project Structure

## Frontend (FinTrack-JavaFE)

```
FinTrack-JavaFE/
├── Main.java              # Application entry point, CardLayout setup
├── components/            # Reusable UI components
│   ├── Sidebar.java       # Navigation sidebar with route handling
│   ├── TopBar.java        # Top header bar
│   ├── InfoBar.java       # Financial summary display bar
│   ├── ActionBar.java     # Quick action buttons (add transaction, date picker)
│   └── PopupCard.java     # Modal dialog component
├── pages/                 # Page-level components
│   ├── HomePage.java      # Dashboard with charts and summaries
│   ├── TransaksiPage.java # Transaction list and management
│   ├── LaporanPage.java   # Reports and analytics
│   ├── AkunWalletPage.java # Account/wallet management
│   ├── KategoriPage.java  # Category management
│   └── SettingsPage.java  # Application settings
├── utils/                 # Utility classes
│   ├── FontUtil.java      # Global font configuration
│   ├── ComboUtil.java     # Custom ComboBox styling
│   ├── ScrollUtil.java    # Custom ScrollBar styling
│   ├── TransactionStore.java # Transaction state management
│   ├── AccountStore.java  # Account state management
│   └── CategoryStore.java # Category state management
└── workspace/             # Additional workspace files (possibly legacy/experimental)
```

## Backend (fintrack-backend2)

```
fintrack-backend2/
├── src/
│   ├── Backend2Facade.java    # Main API entry point
│   ├── Server.java            # Socket server (port 3000)
│   ├── ClientHandler.java     # Per-client connection handler
│   ├── Backend2CliApp.java    # CLI testing tool
│   ├── TestSocketClient.java  # Socket client for testing
│   ├── model/                 # Data models
│   │   ├── User.java          # User entity
│   │   ├── Transaction.java   # Transaction entity (UUID-based)
│   │   └── FinancialSummary.java # Summary calculations
│   ├── service/               # Business logic layer (thread-safe)
│   │   ├── UserManager.java   # User CRUD and authentication
│   │   ├── TransactionService.java # Transaction CRUD and summaries
│   │   └── ReportService.java # CSV report generation
│   ├── persistence/           # Data access layer
│   │   └── FileManager.java   # File I/O for transactions
│   └── test/                  # Test classes
│       ├── TestBackend2Facade.java
│       ├── TestFileManager.java
│       ├── TestTransaction.java
│       └── TestTransactionService.java
├── data/                      # Runtime data directory (create manually)
│   ├── users.txt              # User credentials
│   └── database_transactions.txt # Transaction records
└── reports/                   # Generated CSV reports (create manually)
    └── *.csv
```

## Conventions

### Frontend
- **Navigation**: Route-based with string keys ("dashboard", "transaksi", "laporan", etc.)
- **Component Naming**: PascalCase for classes, camelCase for variables
- **Custom Components**: Inner classes for icons and styled components (e.g., `RoundedButton`, `RoundPanel`)
- **Color Scheme**: Tailwind-inspired palette (slate-50 to slate-800, blue-50 to blue-600)
- **Layout**: BorderLayout for main structure, GridBagLayout for flexible lists, CardLayout for page switching

### Backend
- **Command Protocol**: Pipe-delimited text commands (`COMMAND|param1|param2|...`)
- **Response Format**: Structured responses with status prefix (`OK`, `ERROR`, `SUMMARY`, `DATA_ALL`, `FILE_SIZE`)
- **Data Format**: Pipe-delimited CSV for persistence (`field1|field2|field3`)
- **Thread Safety**: All public service methods are synchronized
- **Error Handling**: Descriptive error codes in responses (`ERROR|USER_EXISTS`, `ERROR|UPDATE_NOT_FOUND`)
- **Session Management**: Username passed with each transaction command (stateless per-command)

### File Organization
- Compiled `.class` files coexist with `.java` sources (no separate build directory)
- No package declarations in most files (default package)
- `utils` package is the only packaged code in frontend
