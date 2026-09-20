# SISTA Enterprise — Database Validation Suite

This directory contains SQL scripts designed to validate the integrity of the SISTA backend database (PostgreSQL).

## Scripts Overview

1. `validate_sync_integrity.sql`: Checks offline-first sync consistency, duplicate idempotency keys, and stale records.
2. `validate_billing_mutations.sql`: Validates payment transitions, Virtual Account (VA) uniqueness, and amount discrepancies.
3. `validate_academic_data.sql`: Verifies schedules, grades (0-100), and CBT exam correctness.

## Expected Results

**A perfectly clean and healthy database should return `0 rows` for all queries in these scripts.** Any rows returned indicate a data integrity issue that requires investigation.

## Usage Instructions

### Using PostgreSQL CLI (`psql`)
Run the scripts directly against your production or staging database:
```bash
psql -U your_user -d sista_db -f qa/database/validate_sync_integrity.sql
psql -U your_user -d sista_db -f qa/database/validate_billing_mutations.sql
psql -U your_user -d sista_db -f qa/database/validate_academic_data.sql
```

### Using Supabase SQL Editor
1. Log in to your Supabase project dashboard.
2. Navigate to the SQL Editor.
3. Paste the contents of each `.sql` file into a new query tab.
4. Run the queries and inspect the results.

### Using PostgreSQL MCP Server
If you are using an MCP server, you can execute these queries by providing the script contents to the database connection tools.
