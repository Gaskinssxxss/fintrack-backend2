# Changelog: CLI Logging Implementation

## Tanggal: 2025-12-02

## Masalah
Logging tidak tersimpan saat mengakses backend melalui CLI (`Backend2CliApp`), hanya bekerja saat akses via socket server.

## Penyebab
`Backend2CliApp` langsung memanggil `backend.handleTextCommand()` tanpa menggunakan `RequestLogger`, sementara `ClientHandler` (socket server) sudah mengintegrasikan logging.

## Solusi

### 1. Modifikasi Backend2CliApp.java

#### Tambahan Import
```java
import util.RequestLogger;
```

#### Tambahan Field
```java
private final RequestLogger logger;
```

#### Inisialisasi di Constructor
```java
public Backend2CliApp() {
    this.backend = new Backend2Facade();
    this.scanner = new Scanner(System.in);
    this.logger = RequestLogger.getInstance();
}
```

#### Helper Method Baru
```java
private String executeCommand(String command) {
    // Log request
    logger.logRequest(command, currentUsername != null ? currentUsername : "GUEST");
    
    // Execute command
    String response = backend.handleTextCommand(command);
    
    // Log response
    if (response.startsWith("ERROR")) {
        logger.logError(command, response);
    } else {
        logger.logSuccess(command, response);
    }
    
    return response;
}
```

#### Update Semua Method
Semua method yang memanggil `backend.handleTextCommand()` diubah menjadi `executeCommand()`:
- `doRegister()`
- `doLogin()`
- `doChangePassword()`
- `doAddTransaction()`
- `doUpdateTransaction()`
- `doDeleteTransaction()`
- `doGetAllTransactions()`
- `doGetSummary()`
- `doReqReportHeader()`

### 2. Update Dokumentasi

#### LOGGING.md
Ditambahkan section "Integration dengan Backend2CliApp (CLI)" yang menjelaskan:
- CLI juga menggunakan logging otomatis
- Helper method `executeCommand()` menangani logging
- Username tracking untuk audit trail

#### CLI_FEATURES.md
Ditambahkan section "Request Logging" yang menjelaskan:
- Semua operasi CLI dicatat ke `logs/request_log.txt`
- Format log sama dengan socket server
- Contoh log entries dari CLI

## Hasil

### Sebelum
```
# CLI tidak mencatat log apapun
# File logs/request_log.txt hanya berisi log dari socket server
```

### Sesudah
```
# Semua operasi CLI dicatat dengan format:
[2025-12-02 23:00:02] COMMAND: REGISTER|testuser|*** | STATUS: RECEIVED | DETAILS: User: GUEST
[2025-12-02 23:00:02] COMMAND: REGISTER | STATUS: SUCCESS | DETAILS: REGISTER_OK|testuser
[2025-12-02 23:00:10] COMMAND: LOGIN|testuser|*** | STATUS: RECEIVED | DETAILS: User: GUEST
[2025-12-02 23:00:10] COMMAND: LOGIN | STATUS: SUCCESS | DETAILS: LOGIN_OK|testuser
```

## Testing

Test dilakukan dengan `TestCliLogging.java`:
```bash
cd fintrack-backend2/src
javac test\TestCliLogging.java
java test.TestCliLogging
```

Output menunjukkan:
- ✓ RequestLogger instance created successfully
- ✓ Test command logged
- ✓ Log file exists and contains entries

## Manfaat

1. **Audit Trail Lengkap**: Semua operasi (CLI dan socket) tercatat
2. **Debugging Lebih Mudah**: Developer bisa trace semua command yang dijalankan
3. **Konsistensi**: Format log sama untuk CLI dan socket server
4. **User Tracking**: Setiap operasi mencatat username yang melakukan aksi

## Cara Menggunakan

```powershell
# Jalankan CLI
cd fintrack-backend2/src
java Backend2CliApp

# Lakukan operasi (register, login, add transaction, dll)

# Lihat log
type ..\logs\request_log.txt
```

## File yang Dimodifikasi

1. `src/Backend2CliApp.java` - Implementasi logging
2. `LOGGING.md` - Dokumentasi logging
3. `CLI_FEATURES.md` - Dokumentasi CLI features
4. `src/test/TestCliLogging.java` - Test logging (baru)

## Backward Compatibility

✓ Tidak ada breaking changes
✓ Semua fitur CLI tetap berfungsi seperti sebelumnya
✓ Logging bersifat tambahan dan tidak mengubah behavior
