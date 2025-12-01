# 🚀 FinTrack - Backend 2 Service Documentation

FinTrack Backend 2 adalah layanan inti yang menangani **logika keuangan** (manajemen transaksi, ringkasan) dan **persistency** (penyimpanan data) untuk aplikasi FinTrack.

Layanan ini dirancang untuk diakses oleh **Backend 1** (Service Layer/API Gateway) melalui dua jenis API utama:

1.  **Text Command API** (melalui `handleTextCommand(String)`).
2.  **Binary Report API** (melalui `getReportBytes()` atau `writeReportToStream(...)`).

Backend 1 bertanggung jawab untuk mengelola koneksi client, sesi (`currentUsername`), dan menerjemahkan permintaan client ke format command string yang diperlukan oleh Backend 2.

---

## 1. 🏗️ Struktur Direktori & File Runtime

Struktur proyek mengikuti pola Service-Persistence-Model.
project-root/ ├─ src/ │ ├─ Backend2Facade.java // ⚡️ Single Entry Point untuk Backend 1 │ ├─ Backend2CliApp.java // CLI interaktif untuk API testing │ │ │ ├─ model/ // Data Model (User, Transaction, FinancialSummary) │ ├─ persistence/ // File I/O (FileManager) │ ├─ service/ // Business Logic (UserManager, TransactionService, ReportService) │ └─ test/ // Test Classes │ ├─ data/ // 📂 Persistence Data (Dibuat otomatis oleh FileManager/UserManager) │ ├─ database_transactions.txt │ └─ users.txt │ └─ reports/ // 📊 Output Laporan CSV ├─ report_test.csv └─ report_cli.csv

> **⚠️ Catatan Penting:**
> Folder **`data/`** dan **`reports/`** **harus dibuat secara manual** di `project-root/` sebelum menjalankan program, karena file ditulis ke path relatif.

---

## 2. 📋 Data Model

### 2.1. `model.User`

- **Penyimpanan:** `data/users.txt`
- **Format Internal (`toCSV()`):** `username|password` (Password disimpan dalam plain text untuk demo).
- **Getter:** `getUsername()`, `getPassword()`

### 2.2. `model.Transaction`

- **Penyimpanan:** `data/database_transactions.txt`
- **Fields:** `id` (UUID), `username`, `date` ("YYYY-MM-DD"), `description`, `category`, `type` ("INCOME"/"EXPENSE"), `amount` (double).
- **Format Internal (`toCSV()`):** `id|username|date|description|category|type|amount`

### 2.3. `model.FinancialSummary`

- Menghitung `totalBalance = totalIncome - totalExpense`.
- **Protokol String (`toProtocolString()`):** `SUMMARY|<balance>|<income>|<expense>` (Angka dibulatkan ke `long`).

---

## 3. ⚙️ Service Layer (Thread-Safe)

Semua method publik yang memodifikasi data di layer `service` dijamin **thread-safe** menggunakan `synchronized`.

### 3.1. `service.UserManager`

- **Tugas:** CRUD User sederhana, validasi login.
- **Data:** Memuat semua user ke `Map<String, User>` dari `data/users.txt` saat inisialisasi.
- **Metode Kunci:** `registerUser()`, `validateLogin()`, `changePassword()`.

### 3.2. `persistence.FileManager`

- **Tugas:** I/O level rendah untuk `data/database_transactions.txt`.
- **Metode Kunci:** `loadTransactions()`, `saveTransaction()` (Append), `saveAll()` (Overwrite).

### 3.3. `service.TransactionService`

- **Tugas:** Business logic CRUD Transaksi dan summary.
- **Data:** Memuat semua transaksi ke `List<Transaction>` saat inisialisasi.
- **Metode Kunci:**
  - **CREATE:** `addTransaction(...)`, `addTransactionFromCommand(...)`
  - **READ:** `getAllTransactions()`, `getSummary()`, `buildAllTransactionsMessage()` (Protokol `DATA_ALL`).
  - **UPDATE/DELETE:** `updateTransaction(...)`, `deleteTransaction(...)` (Sinkronisasi ke file via `FileManager.saveAll()` setelah modifikasi).

### 3.4. `service.ReportService`

- **Tugas:** Konversi `List<Transaction>` menjadi CSV.
- **Metode Kunci:** `buildCsv()`, `buildCsvBytes()` (Encoding: UTF-8).

---

## 4. ⚡️ Backend2Facade – Public API

### 4.1. Text Command Protocol (`handleTextCommand(String line)`)

Semua perintah dipisahkan oleh `|`.

#### 4.1.1. User API

| Command (Request) | Response (Success) | Response (Error) |
| :---------------- | :----------------- | :--------------- | ------------ | ------------------- | --------------------------------------------------------- | ----------------------------------------------------------------------------- |
| `REGISTER         | <uName>            | <pass>`          | `REGISTER_OK | <uName>`            | `ERROR\|USER_EXISTS` / `ERROR\|REGISTER_FORMAT`           |
| `LOGIN            | <uName>            | <pass>`          | `LOGIN_OK    | <uName>`            | `LOGIN_FAIL\|INVALID_CREDENTIALS` / `ERROR\|LOGIN_FORMAT` |
| `CHANGE_PASSWORD  | <uName>            | <oldPass>        | <newPass>`   | `CHANGE_PASSWORD_OK | <uName>`                                                  | `CHANGE_PASSWORD_FAIL\|INVALID_CREDENTIALS` / `ERROR\|CHANGE_PASSWORD_FORMAT` |

> **Catatan Session:** Backend 1 harus menyimpan `<uName>` dari `LOGIN_OK` dan menggunakannya pada command Transaksi (`ADD`, `UPDATE`) dan `CHANGE_PASSWORD` selanjutnya.

#### 4.1.2. Transaction API

| Command (Request) | Response (Success)                   | Response (Error) |
| :---------------- | :----------------------------------- | :--------------- | ------ | ------ | ------ | ------------------------- | --------- | -------- | ----- | ------ | ------ | ------------------------- |
| `ADD              | <uName>                              | <date>           | <desc> | <cat>  | <type> | <amount>`                 | `SUMMARY  | <bal>    | <inc> | <exp>` | N/A    |
| `UPDATE           | <id>                                 | <uName>          | <date> | <desc> | <cat>  | <type>                    | <amount>` | `SUMMARY | <bal> | <inc>  | <exp>` | `ERROR\|UPDATE_NOT_FOUND` |
| `DELETE           | <id>`                                | `SUMMARY         | <bal>  | <inc>  | <exp>` | `ERROR\|DELETE_NOT_FOUND` |
| `GET_SUMMARY`     | `SUMMARY                             | <bal>            | <inc>  | <exp>` | N/A    |
| `GET_ALL`         | `DATA_ALL\|id;date;desc;...#id2;...` | N/A              |

#### 4.1.3. Report API

| Command (Request) | Response (Success) | Deskripsi        |
| :---------------- | :----------------- | :--------------- | ------------------------------------------------------- |
| `REQ_REPORT`      | `FILE_SIZE         | <lengthInBytes>` | Meminta ukuran file CSV (Header untuk streaming biner). |

#### 4.1.4. Error Generic

- `ERROR\|EMPTY_COMMAND`
- `ERROR\|UNKNOWN_COMMAND|<line>`

### 4.2. Binary Report API

Digunakan setelah `REQ_REPORT` untuk transfer file CSV.

- `public byte[] getReportBytes()`: Mengambil seluruh laporan CSV dalam bentuk byte array.
- `public void writeReportToStream(OutputStream out) throws IOException`: Menulis laporan CSV langsung ke `OutputStream` (disarankan untuk streaming ke socket).

---

## 5. 🤝 Panduan Integrasi Backend 1

### Gambaran Tugas

- **Backend 1:** Network Layer, Session Management (`currentUsername`).
- **Backend 2:** Logic & Persistence.

### Alur Session Login dan Transaksi

1.  Client kirim `LOGIN`.
2.  Backend 1 panggil `backend2.handleTextCommand("LOGIN|uName|pass")`.
3.  Jika respons **`LOGIN_OK|username`**, Backend 1 **set `currentUsername`**.
4.  Untuk setiap transaksi selanjutnya: Backend 1 **prepend** `currentUsername` ke command client sebelum diteruskan ke Backend 2.

**Contoh Translasi ADD (Backend 1):**

- **Input Client:** `ADD|2025-12-02|Makan siang|Makanan|EXPENSE|25000.0`
- **`currentUsername`:** `indra`
- **Command ke BE2:** `ADD|indra|2025-12-02|Makan siang|Makanan|EXPENSE|25000.0`

### Alur Report Streaming

1.  Client kirim `REQ_REPORT`.
2.  Backend 1 panggil `String header = backend2.handleTextCommand("REQ_REPORT");` (mendapat `FILE_SIZE|...`).
3.  Backend 1 kirim `header` ke client.
4.  Setelah konfirmasi dari client, Backend 1 langsung panggil:
    ```java
    backend2.writeReportToStream(clientSocket.getOutputStream());
    // Laporan CSV biner dikirim ke client
    ```

---

## 6. 🧪 CLI Tester (`Backend2CliApp`)

`Backend2CliApp.java` mensimulasikan peran Backend 1 + client dalam satu aplikasi CLI, berguna untuk testing manual dan memahami alur pemanggilan `Backend2Facade`.

### Cara Menjalankan

Dari `project-root/`:

1.  **Compile:**
    ```bash
    javac src/*.java src/model/*.java src/service/*.java src/persistence/*.java src/test/*.java
    ```
2.  **Run:**
    ```bash
    cd src
    java Backend2CliApp
    ```

### Alur Rekomendasi Testing

1.  **User API:** `REGISTER` $\rightarrow$ `LOGIN` (set `currentUsername` di CLI).
2.  **Transaction API:** `ADD` beberapa transaksi $\rightarrow$ `GET_SUMMARY` $\rightarrow$ `GET_ALL`.
3.  **Report API:** `REQ_REPORT` dan generate laporan ke `reports/report_cli.csv`.
