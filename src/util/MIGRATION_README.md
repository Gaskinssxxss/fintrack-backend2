# Data Migration Utility

This utility migrates transaction data from the old format to the new format.

## What it does

- **Old format** (7 fields): `id|username|date|description|category|type|amount`
- **New format** (9 fields): `id|username|date|description|category|type|amount|accountName|accountType`

The migration:
1. Creates a timestamped backup of your original file
2. Converts old format transactions to new format
3. Adds default values (`-`) for accountName and accountType if missing
4. Converts double amounts to long integers
5. Preserves transactions already in new format

## How to run

### From command line:

```bash
# Navigate to src directory
cd fintrack-backend2/src

# Compile the utility
javac util/DataMigrationUtil.java model/Transaction.java

# Run the migration
java util.DataMigrationUtil
```

### Output example:

```
=== FinTrack Data Migration Utility ===
This utility will migrate your transaction data to the new format.

Backup created: C:\...\database_transactions_backup_20251202_220219.txt
Read 12 transactions

Migration Summary:
- Old format transactions: 5
- New format transactions: 7
- Converted transactions: 5
- Total transactions: 12

Migration completed successfully!
```

## Safety features

- **Automatic backup**: A timestamped backup is created before any changes
- **Idempotent**: Safe to run multiple times - won't corrupt already migrated data
- **Non-destructive**: Preserves all existing data, only adds missing fields

## Testing

Run the test suite to verify migration behavior:

```bash
javac test/TestDataMigration.java util/DataMigrationUtil.java
java test.TestDataMigration
```

## Requirements

Validates Requirements 3.3 and 4.2 from the backend-frontend-alignment spec:
- 3.3: Backend SHALL save accountName and accountType in CSV format
- 4.2: Backend SHALL store amount as long integer
