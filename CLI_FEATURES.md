# Backend CLI Enhanced Features

## Status Codes

The CLI now displays HTTP-like status codes for every request:

- **200**: Success (OK, LOGIN_OK, SUMMARY, DATA_ALL, FILE_SIZE)
- **400**: Error (ERROR responses)
- **UNKNOWN**: Unrecognized response format

## Enhanced I/O Display

### Color-Coded Status Messages

- **[✓ SUCCESS]** (Green): Operation completed successfully
- **[✗ ERROR]** (Red): Operation failed
- **[ℹ INFO]** (Cyan): Informational messages
- **[⚠ WARNING]** (Yellow): Warning messages

### Request/Response Formatting

Every API call now shows:

```
┌─ REQUEST
│ COMMAND|param1|param2|...
└─

┌─ RESPONSE [STATUS_CODE]
│ Response data...
└─

[✓ SUCCESS] Operation description
```

## Example Output

### Register User
```
--- REGISTER USER ---
Username (contoh: indra): testuser
Password (contoh: 12345): pass123

┌─ REQUEST
│ REGISTER|testuser|pass123
└─

┌─ RESPONSE [200]
│ OK
└─

[✓ SUCCESS] User 'testuser' berhasil didaftarkan
```

### Login
```
--- LOGIN USER ---
Username (contoh: indra): testuser
Password (contoh: 12345): pass123

┌─ REQUEST
│ LOGIN|testuser|pass123
└─

┌─ RESPONSE [200]
│ LOGIN_OK|testuser
└─

[✓ SUCCESS] Login berhasil. User aktif: testuser
```

### Add Transaction
```
--- ADD TRANSACTION ---
Tanggal (YYYY-MM-DD) (contoh: 2025-12-02): 2025-12-02
Deskripsi (contoh: Gaji Bulanan): Salary
Kategori (contoh: Gaji / Makanan / Transport): Income
Pilih Type Transaksi:
1. INCOME  (Pemasukan)
2. EXPENSE (Pengeluaran)
Pilih [1-2]: 1
Jumlah (angka) (contoh: 8500000): 5000000

┌─ REQUEST
│ ADD|testuser|2025-12-02|Salary|Income|INCOME|5000000.0
└─

┌─ RESPONSE [200]
│ SUMMARY|5000000.0|5000000.0|0.0
└─

[✓ SUCCESS] Transaksi berhasil ditambahkan
[ℹ INFO] Summary updated: SUMMARY|5000000.0|5000000.0|0.0
```

### Error Example
```
--- LOGIN USER ---
Username (contoh: indra): wronguser
Password (contoh: 12345): wrongpass

┌─ REQUEST
│ LOGIN|wronguser|wrongpass
└─

┌─ RESPONSE [400]
│ ERROR|INVALID_CREDENTIALS
└─

[✗ ERROR] Login gagal. User aktif tidak berubah
```

## Benefits

1. **Clear Status Tracking**: Every operation shows success/failure immediately
2. **Better Debugging**: Request/response pairs are clearly visible
3. **Professional Output**: Color-coded messages improve readability
4. **HTTP-like Codes**: Familiar status code system (200, 400)
5. **Detailed I/O**: All input/output operations are logged with context

## Request Logging

All CLI operations are now automatically logged to `logs/request_log.txt`:

- Every command executed is recorded with timestamp
- Success/error status is tracked
- Username is logged for audit trail
- Same logging format as socket server

Example log entries from CLI:
```
[2025-12-02 23:00:02] COMMAND: REGISTER|testuser|*** | STATUS: RECEIVED | DETAILS: User: GUEST
[2025-12-02 23:00:02] COMMAND: REGISTER | STATUS: SUCCESS | DETAILS: REGISTER_OK|testuser
[2025-12-02 23:00:10] COMMAND: LOGIN|testuser|*** | STATUS: RECEIVED | DETAILS: User: GUEST
[2025-12-02 23:00:10] COMMAND: LOGIN | STATUS: SUCCESS | DETAILS: LOGIN_OK|testuser
[2025-12-02 23:00:15] COMMAND: ADD|testuser|2025-12-02|... | STATUS: RECEIVED | DETAILS: User: testuser
[2025-12-02 23:00:15] COMMAND: ADD | STATUS: SUCCESS | DETAILS: SUMMARY|5000000.0|5000000.0|0.0
```

See [LOGGING.md](LOGGING.md) for more details about the logging system.

## Running the CLI

```powershell
cd fintrack-backend2/src
java Backend2CliApp
```

The enhanced CLI provides a much better testing and debugging experience with clear visibility into all backend operations.
