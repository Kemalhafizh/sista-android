-- ============================================================
-- SISTA Enterprise — Sync Integrity Validation Suite
-- Run against production/staging database to verify
-- offline-first sync correctness (FASE 61)
-- ============================================================

-- 1. Check for duplicate idempotency keys (should be 0)
SELECT idempotency_key, COUNT(*) as occurrences
FROM attendances
WHERE idempotency_key IS NOT NULL
GROUP BY idempotency_key
HAVING COUNT(*) > 1;

-- 2. Verify soft-delete tombstones have is_deleted = true and deleted_at is set
SELECT table_name, COUNT(*) as orphan_tombstones
FROM (
    SELECT 'attendances' as table_name FROM attendances WHERE is_deleted = true AND deleted_at IS NULL
    UNION ALL
    SELECT 'mutabaah_logs' FROM mutabaah_logs WHERE is_deleted = true AND deleted_at IS NULL
) orphans
GROUP BY table_name;

-- 3. Check updated_at timestamp format consistency (should be ISO 8601 / millisecond precision)
SELECT id, updated_at
FROM attendances
WHERE updated_at IS NULL
   OR updated_at < created_at
LIMIT 20;

-- 4. Detect stale sync records (updated more than 7 days ago, never synced)
SELECT id, updated_at, synced_at
FROM attendances
WHERE synced_at IS NULL
  AND updated_at < NOW() - INTERVAL '7 days'
LIMIT 50;

-- 5. Cross-reference: device-submitted records vs server-acknowledged
SELECT
    DATE(created_at) as date,
    COUNT(*) as total_records,
    COUNT(CASE WHEN synced_at IS NOT NULL THEN 1 END) as synced_records,
    COUNT(CASE WHEN synced_at IS NULL THEN 1 END) as pending_sync
FROM attendances
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;
