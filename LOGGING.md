# Sistem Logging Backend

## Overview

Backend FinTrack dilengkapi dengan sistem logging otomatis yang mencatat semua request yang masuk beserta statusnya.

## Lokasi Log File

Log disimpan di: `fintrack-backend2/logs/request_log.txt`

## Format Log Entry

```
[YYYY-MM-DD HH:mm:ss] COMMAND: <command> | STATUS: <status> | DETAILS: <details>
```

### Contoh Log Entry

```
[2025-12-02 22:30:15] COMMAND: LOGIN|john | STATUS: RECEIVED | DETAILS: User: ANONYMOUS
[2025-12-02 22:30:15] COMMAND: LOGIN | STATUS: SUCCESS | DETAILS: User logged in: john
[2025-12-02 22:30:20] COMMAND: ADD | STATUS: RECEIVED | DETAILS: User: john
[2025-12-02 22:30:20] COMMAND: ADD | STATUS: SUCCESS | DETAILS: Response: OK|Transaction added successfully
[2025-12-02 22:30:20] COMMAND: ADD | STATUS: INFO | DETAILS: Broadcast triggered for data change
[2025-12-02 22:30:25] COMMAND: GET_SUMMARY | STATUS: RECEIVED | DETAILS: User: john
[2025-12-02 22:30:25] COMMAND: GET_SUMMARY | STATUS: SUCCESS | DETAILS: Response: SUMMARY|1000000.0|500000.0|500000.0
[2025-12-02 22:30:30] COMMAND: INVALID_CMD | STATUS: RECEIVED | DETAILS: User: john
[2025-12-02 22:30:30] COMMAND: INVALID_CMD | STATUS: ERROR | DETAILS: ERROR|Unknown command
```

## Status Types

- **RECEIVED**: Request diterima oleh server
- **SUCCESS**: Request berhasil diproses
- **ERROR**: Terjadi error saat memproses request
- **INFO**: Informasi tambahan (broadcast, dll)

## Fitur Logging

1. **Automatic Logging**: Semua request otomatis dicatat
2. **User Tracking**: Mencatat username yang melakukan request
3. **Status Tracking**: Mencatat apakah request berhasil atau gagal
4. **Timestamp**: Setiap log entry memiliki timestamp yang akurat
5. **Thread-Safe**: Logging aman untuk concurrent requests
6. **Error Handling**: Jika logging gagal, tidak akan mengganggu operasi server

## Implementasi

### RequestLogger Class

Singleton class yang menangani semua operasi logging:

```java
RequestLogger logger = RequestLogger.getInstance();

// Log incoming request
logger.logRequest(command, username);

// Log success
logger.logSuccess(command, "Operation completed");

// Log error
logger.logError(command, "Error message");

// Log info
logger.logInfo(command, "Additional info");
```

### Integration dengan ClientHandler (Socket Server)

Logging terintegrasi di `ClientHandler.handle()` method:
- Setiap request yang masuk dicatat
- Response status dicatat (OK, ERROR, dll)
- Broadcast events dicatat
- Exception dicatat dengan detail error

### Integration dengan Backend2CliApp (CLI)

Logging juga terintegrasi di `Backend2CliApp`:
- Semua command yang dijalankan via CLI dicatat
- Response status dicatat (SUCCESS, ERROR)
- Username tracking untuk setiap operasi
- Helper method `executeCommand()` menangani logging otomatis

## Maintenance

### Membersihkan Log File

Log file akan terus bertambah seiring waktu. Untuk membersihkan:

```powershell
# Hapus log file
del fintrack-backend2\logs\request_log.txt

# Atau backup dan hapus
move fintrack-backend2\logs\request_log.txt fintrack-backend2\logs\request_log_backup.txt
```

### Monitoring Log

Untuk melihat log secara real-time:

```powershell
# Windows PowerShell
Get-Content fintrack-backend2\logs\request_log.txt -Wait -Tail 20

# CMD
type fintrack-backend2\logs\request_log.txt
```

## Security Notes

- Log file tidak berisi password atau data sensitif
- Log file disimpan di direktori lokal (tidak di-commit ke git)
- Username dicatat untuk audit trail
- Response content di-truncate untuk menghindari log file terlalu besar

## Troubleshooting

### Log File Tidak Terbuat

Pastikan direktori `logs/` ada:
```powershell
mkdir fintrack-backend2\logs
```

### Permission Error

Pastikan aplikasi memiliki write permission ke direktori `logs/`

### Log File Terlalu Besar

Implementasikan log rotation atau hapus log file lama secara berkala
