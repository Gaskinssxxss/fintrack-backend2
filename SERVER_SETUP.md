# 🚀 Panduan Menjalankan FinTrack Backend Server

Panduan cepat untuk menjalankan server backend FinTrack.

---

## 📋 Prasyarat

- **Java Development Kit (JDK)** versi 8 atau lebih tinggi
- **Terminal/Command Prompt** (PowerShell untuk Windows)

Cek instalasi Java:
```bash
java -version
javac -version
```

---

## 🏗️ Setup Awal (Hanya Sekali)

### 1. Buat Direktori Data

Server memerlukan folder `data/` dan `reports/` di root project. Buat secara manual:

**Windows (PowerShell):**
```powershell
cd "c:\Users\ASUS\Documents\Code\fintrack-backend2"
New-Item -ItemType Directory -Path "data" -Force
New-Item -ItemType Directory -Path "reports" -Force
```

**Linux/Mac:**
```bash
cd /path/to/fintrack-backend2
mkdir -p data reports
```

### 2. Struktur Direktori

Setelah setup, struktur project:
```
fintrack-backend2/
├── src/
│   ├── Server.java
│   ├── ClientHandler.java
│   ├── Backend2Facade.java
│   ├── model/
│   ├── service/
│   └── persistence/
├── data/              # ← Dibuat manual (file data akan otomatis)
│   ├── users.txt      # ← Dibuat otomatis saat register user pertama
│   ├── database_transactions.txt  # ← Dibuat otomatis
│   ├── accounts.txt   # ← Dibuat otomatis
│   └── categories.txt # ← Dibuat otomatis
└── reports/           # ← Dibuat manual (CSV reports)
```

---

## ▶️ Menjalankan Server

### Langkah 1: Compile Semua File

**Windows (PowerShell):**
```powershell
cd "c:\Users\ASUS\Documents\Code\fintrack-backend2\src"
javac Server.java ClientHandler.java Backend2Facade.java model\*.java persistence\*.java service\*.java
```

**Linux/Mac:**
```bash
cd /path/to/fintrack-backend2/src
javac Server.java ClientHandler.java Backend2Facade.java model/*.java persistence/*.java service/*.java
```

### Langkah 2: Jalankan Server

```bash
java Server
```

**Output yang diharapkan:**
```
Server started on port 3000
```

Server sekarang berjalan dan mendengarkan koneksi di **port 3000**.

---

## 🧪 Testing Server

### Opsi 1: Menggunakan CLI Tester (Tanpa Frontend)

CLI tester berguna untuk testing manual tanpa perlu menjalankan frontend.

**Compile dan jalankan:**
```powershell
# Dari direktori src/
javac Backend2CliApp.java
java Backend2CliApp
```

**Contoh testing flow:**
1. Register user: `REGISTER|testuser|password123`
2. Login: `LOGIN|testuser|password123`
3. Tambah transaksi: `ADD|testuser|2025-12-02|Makan siang|Makanan|EXPENSE|25000`
4. Lihat summary: `GET_SUMMARY`
5. Lihat semua transaksi: `GET_ALL`
6. Generate report: `REQ_REPORT`

### Opsi 2: Menggunakan Frontend

Jalankan aplikasi frontend FinTrack-JavaFE yang akan otomatis terhubung ke server di port 3000.

---

## 🔧 Troubleshooting

### Error: "Address already in use"
Port 3000 sudah digunakan. Matikan proses yang menggunakan port tersebut atau ubah port di `Server.java`:
```java
public static void main(String[] args) throws IOException {
    new Server(3001).start(); // Ganti ke port lain
}
```

### Error: "Cannot find data directory"
Pastikan folder `data/` dan `reports/` sudah dibuat di root project (bukan di dalam `src/`).

### Error: Compilation failed
Pastikan semua file di-compile dari direktori `src/`:
```powershell
cd src
javac *.java model\*.java service\*.java persistence\*.java
```

### File data tidak terbuat otomatis
File `users.txt`, `database_transactions.txt`, dll akan dibuat otomatis saat:
- User pertama register (users.txt)
- Transaksi pertama ditambahkan (database_transactions.txt)
- Account pertama ditambahkan (accounts.txt)
- Category pertama ditambahkan (categories.txt)

---

## 📡 Protocol Komunikasi

Server menggunakan protokol text-based dengan delimiter `|` (pipe).

### Format Command Utama:

| Command | Format | Response |
|---------|--------|----------|
| Register | `REGISTER\|username\|password` | `REGISTER_OK\|username` |
| Login | `LOGIN\|username\|password` | `LOGIN_OK\|username` |
| Add Transaction | `ADD\|username\|date\|desc\|category\|type\|amount\|account\|accountType` | `SUMMARY\|balance\|income\|expense` |
| Get All | `GET_ALL` | `DATA_ALL\|count\|id;date;desc;...` |
| Get Summary | `GET_SUMMARY` | `SUMMARY\|balance\|income\|expense` |
| Get Accounts | `GET_ACCOUNTS` | `DATA_ACCOUNTS\|count\|id\|name\|...` |
| Get Categories | `GET_CATEGORIES` | `DATA_CATEGORIES\|count\|type\|name\|...` |

### Broadcast Events:
Server akan broadcast ke semua client yang terhubung saat ada perubahan data:
- `REFRESH_TABLE` - Saat ada transaksi baru/update/delete
- `SUMMARY|...` - Summary terbaru setelah perubahan

---

## 🛑 Menghentikan Server

Tekan `Ctrl+C` di terminal untuk menghentikan server.

---

## 📚 Dokumentasi Lengkap

Untuk dokumentasi API lengkap dan detail arsitektur, lihat [README.md](./README.md).

---

## 🔗 Quick Reference

**Lokasi Project:**
- Backend: `c:\Users\ASUS\Documents\Code\fintrack-backend2`
- Frontend: `c:\Users\ASUS\Documents\Code\FinTrack-JavaFE`

**Port Server:** 3000

**File Data:**
- Users: `data/users.txt`
- Transactions: `data/database_transactions.txt`
- Accounts: `data/accounts.txt`
- Categories: `data/categories.txt`
- Reports: `reports/*.csv`
