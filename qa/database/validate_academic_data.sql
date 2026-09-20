-- ============================================================
-- SISTA Enterprise — Academic Data Validation Suite
-- Verifies schedule, grade, and CBT exam data integrity
-- ============================================================

-- 1. Orphan schedules (schedule exists but teacher doesn't exist)
SELECT s.id, s.subject_name, s.teacher_id, s.day_of_week
FROM schedules s
LEFT JOIN teachers t ON t.id = s.teacher_id
WHERE t.id IS NULL;

-- 2. Grade range validation (all scores should be 0-100)
SELECT g.id, g.student_id, g.subject, g.score, g.type
FROM grades g
WHERE g.score < 0 OR g.score > 100;

-- 3. Duplicate CBT answers (one student should have max 1 answer per question)
SELECT student_id, question_id, COUNT(*) as answer_count
FROM cbt_answers
GROUP BY student_id, question_id
HAVING COUNT(*) > 1;

-- 4. CBT exam integrity (submitted exams should have answers for all questions)
SELECT e.id as exam_id, e.title,
       eq.total_questions,
       COALESCE(sa.answered_count, 0) as answered_count
FROM cbt_exams e
CROSS JOIN LATERAL (
    SELECT COUNT(*) as total_questions FROM cbt_questions q WHERE q.exam_id = e.id
) eq
LEFT JOIN LATERAL (
    SELECT student_id, COUNT(*) as answered_count
    FROM cbt_answers a
    WHERE a.exam_id = e.id
    GROUP BY student_id
    HAVING COUNT(*) < eq.total_questions
) sa ON true
WHERE sa.student_id IS NOT NULL;

-- 5. Schedule time conflict detection (same teacher, same time slot, same day)
SELECT s1.id as schedule_1, s2.id as schedule_2,
       s1.teacher_id, s1.day_of_week,
       s1.start_time, s1.end_time
FROM schedules s1
JOIN schedules s2 ON s1.teacher_id = s2.teacher_id
  AND s1.day_of_week = s2.day_of_week
  AND s1.id < s2.id
  AND s1.start_time < s2.end_time
  AND s2.start_time < s1.end_time;
