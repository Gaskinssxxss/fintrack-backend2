# Technology Stack

## Frontend (FinTrack-JavaFE)

**Language**: Java (Swing)

**UI Framework**: Java Swing with custom components
- Custom rounded buttons, panels, and navigation
- Material Design-inspired color palette (slate, blue tones)
- Custom icons and charts rendered with Graphics2D
- CardLayout for page navigation

**Key Libraries/Utilities**:
- `FontUtil`: Global font management (Segoe UI, San Francisco fallback chain)
- `ComboUtil`, `ScrollUtil`: Custom UI component styling
- Store pattern: `TransactionStore`, `AccountStore`, `CategoryStore` for state management

**Project Structure**:
- `Main.java`: Application entry point with CardLayout navigation
- `components/`: Reusable UI components (Sidebar, TopBar, InfoBar, ActionBar, PopupCard)
- `pages/`: Page components (HomePage, TransaksiPage, LaporanPage, AkunWalletPage, KategoriPage, SettingsPage)
- `utils/`: Utility classes and data stores

## Backend (fintrack-backend2)

**Language**: Java (Standard Edition)

**Architecture Pattern**: Service-Persistence-Model (3-layer)
- `model/`: Data models (User, Transaction, FinancialSummary)
- `persistence/`: File I/O (FileManager)
- `service/`: Business logic (UserManager, TransactionService, ReportService)
- `Backend2Facade`: Single entry point API for frontend

**Communication Protocol**:
- Text-based command protocol over TCP sockets (port 3000)
- Pipe-delimited commands: `COMMAND|param1|param2|...`
- Binary streaming for CSV report downloads

**Data Storage**:
- Flat file persistence in `data/` directory
- `users.txt`: User credentials (pipe-delimited)
- `database_transactions.txt`: Transaction records (pipe-delimited)
- CSV format for reports in `reports/` directory

**Thread Safety**: All service methods use `synchronized` for concurrent access

## Common Commands

### Frontend Compilation & Execution

```powershell
# From FinTrack-JavaFE directory
javac Main.java components/*.java pages/*.java utils/*.java
java Main
```

### Backend Compilation & Execution

```powershell
# From fintrack-backend2/src directory
javac *.java model/*.java service/*.java persistence/*.java
java Server
```

Or compile all at once:
```powershell
javac Server.java ClientHandler.java Backend2Facade.java model\*.java persistence\*.java service\*.java
```

### Testing Backend CLI

```powershell
# From fintrack-backend2/src directory
java Backend2CliApp
```

## Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher
- **Manual Setup**: Create `data/` and `reports/` directories in backend project root before first run
