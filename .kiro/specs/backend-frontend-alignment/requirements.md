# Requirements Document

## Introduction

Sistem FinTrack memerlukan backend yang menyediakan semua fungsi yang dibutuhkan oleh frontend Java Swing. Saat ini terdapat ketidaksesuaian antara data yang dibutuhkan frontend dengan yang disediakan backend, terutama terkait dengan:
- Manajemen akun/wallet (belum ada di backend)
- Manajemen kategori (belum ada di backend)
- Format data transaksi yang berbeda (frontend menggunakan `accountName`, `accountType`, backend belum mendukung)
- Tipe data amount (frontend menggunakan `long`, backend menggunakan `double`)

Fitur ini akan memastikan backend menyediakan semua API yang dibutuhkan frontend dengan format data yang konsisten.

## Glossary

- **Backend**: Server Java yang menangani logika bisnis dan persistensi data melalui socket TCP port 3000
- **Frontend**: Aplikasi desktop Java Swing yang berkomunikasi dengan Backend
- **Transaction**: Catatan transaksi keuangan (pemasukan atau pengeluaran)
- **Account**: Akun keuangan (Bank, E-Wallet, Cash, Kredit) dengan saldo
- **Category**: Kategori transaksi (Makanan, Transportasi, Gaji, dll)
- **Command Protocol**: Format komunikasi text-based dengan delimiter pipe (|)
- **TransactionStore**: Store pattern di frontend untuk manajemen state transaksi
- **AccountStore**: Store pattern di frontend untuk manajemen state akun
- **CategoryStore**: Store pattern di frontend untuk manajemen state kategori
- **Session**: Sesi autentikasi user yang aktif dengan session token unik
- **SessionManager**: Komponen yang mengelola sesi user yang sedang login

## Requirements

### Requirement 1

**User Story:** Sebagai frontend developer, saya ingin backend menyediakan API manajemen akun, sehingga pengguna dapat mengelola berbagai akun keuangan mereka.

#### Acceptance Criteria

1. WHEN frontend mengirim command ADD_ACCOUNT dengan parameter name, number, balance, dan type THEN Backend SHALL membuat akun baru dan mengembalikan response OK dengan ID akun
2. WHEN frontend mengirim command GET_ACCOUNTS THEN Backend SHALL mengembalikan semua akun dalam format DATA_ACCOUNTS dengan field id, name, number, balance, dan type
3. WHEN frontend mengirim command UPDATE_ACCOUNT dengan parameter id, name, number, balance, dan type THEN Backend SHALL memperbarui akun dan mengembalikan response OK
4. WHEN frontend mengirim command DELETE_ACCOUNT dengan parameter id THEN Backend SHALL menghapus akun dan mengembalikan response OK
5. WHEN Backend menyimpan akun THEN Backend SHALL menggunakan tipe data long untuk balance agar konsisten dengan frontend

### Requirement 2

**User Story:** Sebagai frontend developer, saya ingin backend menyediakan API manajemen kategori, sehingga pengguna dapat mengelola kategori transaksi mereka.

#### Acceptance Criteria

1. WHEN frontend mengirim command ADD_CATEGORY dengan parameter type dan name THEN Backend SHALL membuat kategori baru dan mengembalikan response OK
2. WHEN frontend mengirim command GET_CATEGORIES THEN Backend SHALL mengembalikan semua kategori dalam format DATA_CATEGORIES dengan field type dan name
3. WHEN frontend mengirim command DELETE_CATEGORY dengan parameter type dan name THEN Backend SHALL menghapus kategori dan mengembalikan response OK
4. WHEN Backend menyimpan kategori THEN Backend SHALL membedakan kategori income dan expense berdasarkan type field

### Requirement 3

**User Story:** Sebagai frontend developer, saya ingin backend menyimpan informasi akun pada setiap transaksi, sehingga transaksi dapat dikaitkan dengan akun tertentu.

#### Acceptance Criteria

1. WHEN frontend mengirim command ADD dengan parameter tambahan accountName dan accountType THEN Backend SHALL menyimpan transaksi dengan informasi akun
2. WHEN Backend mengembalikan data transaksi THEN Backend SHALL menyertakan field accountName dan accountType
3. WHEN Backend menyimpan transaksi ke file THEN Backend SHALL menyimpan accountName dan accountType dalam format CSV
4. WHEN Backend membaca transaksi dari file THEN Backend SHALL memuat accountName dan accountType dengan benar

### Requirement 4

**User Story:** Sebagai frontend developer, saya ingin backend menggunakan tipe data long untuk amount, sehingga konsisten dengan frontend dan menghindari masalah floating point.

#### Acceptance Criteria

1. WHEN Backend menerima command dengan amount THEN Backend SHALL mem-parse amount sebagai long integer
2. WHEN Backend menyimpan transaksi THEN Backend SHALL menyimpan amount sebagai long integer
3. WHEN Backend mengembalikan data transaksi THEN Backend SHALL mengirim amount sebagai long integer
4. WHEN Backend melakukan kalkulasi summary THEN Backend SHALL menggunakan long integer untuk menghindari kesalahan pembulatan

### Requirement 5

**User Story:** Sebagai frontend developer, saya ingin backend menyediakan API untuk mendapatkan summary keuangan per bulan, sehingga dashboard dapat menampilkan trend 6 bulan terakhir.

#### Acceptance Criteria

1. WHEN frontend mengirim command GET_MONTHLY_SUMMARY dengan parameter username dan yearMonth THEN Backend SHALL mengembalikan income, expense, dan balance untuk bulan tersebut
2. WHEN Backend menghitung monthly summary THEN Backend SHALL mengelompokkan transaksi berdasarkan year dan month
3. WHEN Backend mengembalikan monthly summary THEN Backend SHALL menggunakan format MONTHLY_SUMMARY dengan field yearMonth, income, expense, dan balance

### Requirement 6

**User Story:** Sebagai frontend developer, saya ingin backend menyediakan API untuk mendapatkan breakdown kategori pengeluaran, sehingga pie chart dapat ditampilkan.

#### Acceptance Criteria

1. WHEN frontend mengirim command GET_CATEGORY_BREAKDOWN dengan parameter username dan yearMonth THEN Backend SHALL mengembalikan total pengeluaran per kategori
2. WHEN Backend menghitung category breakdown THEN Backend SHALL hanya menghitung transaksi dengan type EXPENSE
3. WHEN Backend mengembalikan category breakdown THEN Backend SHALL menggunakan format CATEGORY_BREAKDOWN dengan field category dan amount

### Requirement 7

**User Story:** Sebagai system architect, saya ingin backend menggunakan model data yang konsisten, sehingga mudah dipelihara dan dikembangkan.

#### Acceptance Criteria

1. WHEN Backend membuat model Account THEN Backend SHALL menggunakan field id, name, number, balance (long), dan type
2. WHEN Backend membuat model Category THEN Backend SHALL menggunakan field type dan name
3. WHEN Backend memperbarui model Transaction THEN Backend SHALL menambahkan field accountName dan accountType serta mengubah amount dari double ke long
4. WHEN Backend menyimpan data ke file THEN Backend SHALL menggunakan format pipe-delimited CSV yang konsisten

### Requirement 8

**User Story:** Sebagai frontend developer, saya ingin backend menyediakan API untuk filter transaksi, sehingga pengguna dapat mencari transaksi berdasarkan kriteria tertentu.

#### Acceptance Criteria

1. WHEN frontend mengirim command FILTER_TRANSACTIONS dengan parameter date, category, dan account THEN Backend SHALL mengembalikan transaksi yang sesuai dengan filter
2. WHEN Backend memfilter transaksi THEN Backend SHALL mendukung filter optional (jika parameter kosong, tidak difilter)
3. WHEN Backend mengembalikan hasil filter THEN Backend SHALL menggunakan format DATA_ALL yang sama dengan GET_ALL

### Requirement 9

**User Story:** Sebagai system administrator, saya ingin backend menyimpan data dengan thread-safe, sehingga multiple client dapat mengakses secara bersamaan tanpa data corruption.

#### Acceptance Criteria

1. WHEN multiple client mengakses Backend secara bersamaan THEN Backend SHALL menggunakan synchronized method untuk semua operasi data
2. WHEN Backend menulis ke file THEN Backend SHALL menggunakan file locking untuk mencegah race condition
3. WHEN Backend membaca dari file THEN Backend SHALL menggunakan synchronized method untuk konsistensi data

### Requirement 10

**User Story:** Sebagai frontend developer, saya ingin backend menyediakan error handling yang jelas, sehingga frontend dapat menampilkan pesan error yang informatif kepada pengguna.

#### Acceptance Criteria

1. WHEN Backend menerima command dengan format tidak valid THEN Backend SHALL mengembalikan ERROR dengan kode error yang spesifik
2. WHEN Backend gagal melakukan operasi THEN Backend SHALL mengembalikan ERROR dengan deskripsi yang jelas
3. WHEN Backend berhasil melakukan operasi THEN Backend SHALL mengembalikan response OK atau data yang diminta
4. WHEN Backend mengembalikan error THEN Backend SHALL menggunakan format ERROR|ERROR_CODE|description

### Requirement 11

**User Story:** Sebagai pengguna aplikasi, saya ingin sistem menjaga sesi login saya, sehingga saya tidak perlu login berulang kali dan data saya tetap aman.

#### Acceptance Criteria

1. WHEN user berhasil login THEN Backend SHALL membuat session token unik dan mengembalikannya ke frontend
2. WHEN frontend mengirim command yang memerlukan autentikasi THEN Backend SHALL memvalidasi session token sebelum memproses command
3. WHEN session token tidak valid atau expired THEN Backend SHALL mengembalikan ERROR dengan kode SESSION_INVALID
4. WHEN user logout THEN Backend SHALL menghapus session token dari SessionManager
5. WHEN session tidak digunakan selama periode tertentu THEN Backend SHALL otomatis menghapus session (session timeout)
6. WHEN Backend menyimpan session THEN Backend SHALL menggunakan thread-safe data structure untuk concurrent access
7. WHEN user login dari client yang berbeda THEN Backend SHALL membuat session baru tanpa menghapus session lama (multi-device support)