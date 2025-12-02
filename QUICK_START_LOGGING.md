# Quick Start: Logging di FinTrack Backend

## Apa itu Logging?

Logging adalah fitur yang mencatat semua aktivitas yang terjadi di backend, baik dari CLI maupun socket server. Semua request dan response dicatat dengan timestamp, username, dan status.

## Lokasi Log File

```
fintrack-backend2/logs/request_log.txt
```

## Cara Melihat Log

### Windows PowerShell
```powershell
# Lihat seluruh isi log
Get-Content fintrack-backend2\logs\request_log.txt

# Lihat 20 baris terakhir
Get-Content fintrack-backend2\logs\request_log.txt -Tail 20

# Monitor log secara real-time
Get-Content fintrack-backend2\logs\request_log.txt -Wait -Tail 20
```

### Windows CMD
```cmd
# Lihat seluruh isi log
type fintrack-backend2\logs\request_log.txt

# Lihat baris terakhir (dengan more)
type fintrack-backend2\logs\request_log.txt | more
```

## Format Log Entry

```
[YYYY-MM-DD HH:mm:ss] COMMAND: <command> | STATUS: <status> | DETAILS: <details>
```

### Contoh
```
[2025-12-02 23:00:10] COMMAND: LOGIN|testuser | STATUS: RECEIVED | DETAILS: User: GUEST
[2025-12-02 23:00:10] COMMAND: LOGIN | STATUS: SUCCESS | DETAILS: LOGIN_OK|testuser
[2025-12-02 23:00:15] COMMAND: ADD|testuser|2025-12-02|... | STATUS: RECEIVED | DETAILS: User: testuser
[2025-12-02 23:00:15] COMMAND: ADD | STATUS: SUCCESS | DETAILS: SUMMARY|5000000.0|5000000.0|0.0
```

## Status Types

| Status | Deskripsi |
|--------|-----------|
| RECEIVED | Request diterima oleh backend |
| SUCCESS | Request berhasil diproses |
| ERROR | Terjadi error saat memproses |
| INFO | Informasi tambahan (broadcast, dll) |

## Kapan Logging Terjadi?

### CLI (Backend2CliApp)
✓ Semua command yang dijalankan via CLI dicatat otomatis
✓ Username tracking (GUEST sebelum login, username setelah login)

### Socket Server (ClientHandler)
✓ Semua request dari client dicatat otomatis
✓ Broadcast events dicatat
✓ Exception dicatat dengan detail error

## Testing Logging

### Via CLI
```powershell
cd fintrack-backend2/src
java Backend2CliApp

# Lakukan beberapa operasi:
# 1. Register user
# 2. Login
# 3. Add transaction
# 4. Get summary

# Keluar dari CLI, lalu cek log:
type ..\logs\request_log.txt
```

### Via Test
```powershell
cd fintrack-backend2/src
java test.TestCliLogging
```

## Maintenance

### Backup Log
```powershell
# Backup log file
copy fintrack-backend2\logs\request_log.txt fintrack-backend2\logs\request_log_backup_2025-12-02.txt
```

### Clear Log
```powershell
# Hapus log file (akan dibuat ulang otomatis)
del fintrack-backend2\logs\request_log.txt
```

### Rotate Log (Manual)
```powershell
# Pindahkan log lama dan mulai fresh
move fintrack-backend2\logs\request_log.txt fintrack-backend2\logs\request_log_old.txt
```

## Troubleshooting

### Log File Tidak Ada
```powershell
# Buat direktori logs jika belum ada
mkdir fintrack-backend2\logs
```

### Permission Error
Pastikan aplikasi memiliki write permission ke direktori `logs/`

### Log Tidak Muncul
1. Pastikan sudah compile ulang: `javac Backend2CliApp.java`
2. Pastikan menggunakan versi terbaru dari Backend2CliApp
3. Cek console output untuk error message

## Security Notes

- ✓ Password tidak dicatat di log (diganti dengan ***)
- ✓ Log file tidak di-commit ke git (.gitignore)
- ✓ Username dicatat untuk audit trail
- ✓ Response di-truncate untuk menghindari log terlalu besar

## Dokumentasi Lengkap

- [LOGGING.md](LOGGING.md) - Dokumentasi lengkap sistem logging
- [CLI_FEATURES.md](CLI_FEATURES.md) - Fitur-fitur CLI termasuk logging
- [CHANGELOG_CLI_LOGGING.md](CHANGELOG_CLI_LOGGING.md) - Detail implementasi logging di CLI
