-- ============================================================
-- SISTA Enterprise — Billing Mutation Validation Suite
-- Validates payment data integrity and VA number correctness
-- ============================================================

-- 1. Detect invalid payment status transitions (PAID should never revert to PENDING)
SELECT b.id, b.student_id, b.status, b.updated_at,
       LAG(b.status) OVER (PARTITION BY b.id ORDER BY b.updated_at) as previous_status
FROM billing_history b
WHERE b.status = 'pending'
  AND EXISTS (
    SELECT 1 FROM billing_history bh
    WHERE bh.id = b.id AND bh.status = 'paid' AND bh.updated_at < b.updated_at
  );

-- 2. Check VA number uniqueness per student per billing period
SELECT va_number, COUNT(*) as duplicate_count
FROM billings
WHERE va_number IS NOT NULL
GROUP BY va_number
HAVING COUNT(*) > 1;

-- 3. Validate amount consistency (invoice amount = payment amount)
SELECT b.id, b.student_id, b.amount as invoice_amount,
       p.amount as payment_amount,
       ABS(b.amount - COALESCE(p.amount, 0)) as discrepancy
FROM billings b
LEFT JOIN payments p ON p.billing_id = b.id
WHERE b.status = 'paid'
  AND ABS(b.amount - COALESCE(p.amount, 0)) > 0;

-- 4. Orphan payments (payment exists but no matching billing)
SELECT p.id, p.billing_id, p.amount, p.created_at
FROM payments p
LEFT JOIN billings b ON b.id = p.billing_id
WHERE b.id IS NULL;

-- 5. Billing period coverage check (ensure all active students have billing records)
SELECT s.id as student_id, s.name, s.class
FROM students s
WHERE s.is_active = true
  AND NOT EXISTS (
    SELECT 1 FROM billings b
    WHERE b.student_id = s.id
      AND b.period = TO_CHAR(CURRENT_DATE, 'YYYY-MM')
  )
ORDER BY s.class, s.name;
