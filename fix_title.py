import os
import re

files = [
    "app/src/main/java/com/sultanagung1/sista/ui/academic/RemedialScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/academic/RaporDetailScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/counseling/StudentCounselingScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/counseling/CounselingSessionFormScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/counseling/CounselingDashboardScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/calendar/EventDetailScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/calendar/AcademicCalendarScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/notifications/NotificationSettingsScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/schoolops/UksScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/uks/HealthHistoryScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/uks/UksVisitScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/schoolops/TeachingJournalScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/schoolops/SpmbMobileScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/schoolops/AcademicCalendarScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/elearning/AssignmentSubmitScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/gamification/LeaderboardScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/teacher/JournalFormScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/elearning/ElearningClassDetailScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/gamification/GamificationDashboardScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/teacher/DailyAssessmentScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/elearning/ElearningClassListScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/profile/StudentProfileComprehensiveScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/parent/ChildActivityFeedScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/gamification/BadgeCollectionScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/teacher/AutoGenerateExamScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/teacher/TeachingJournalMobileScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/teacher/QuestionBankScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/teacher/ScoreInputScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/spmb/SpmbRegistrationScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/spmb/SpmbInfoScreen.kt",
    "app/src/main/java/com/sultanagung1/sista/ui/spmb/SpmbTrackingScreen.kt",
]

base_path = "c:\\project\\portofolio\\project-super-web\\sista-android\\"

def parse_balanced(s, start_idx):
    if s[start_idx] != '(':
        return -1
    count = 1
    i = start_idx + 1
    while i < len(s):
        if s[i] == '(':
            count += 1
        elif s[i] == ')':
            count -= 1
            if count == 0:
                return i
        i += 1
    return -1

for filepath in files:
    full_path = os.path.join(base_path, filepath)
    if not os.path.exists(full_path):
        continue
    with open(full_path, 'r', encoding='utf-8') as f:
        content = f.read()

    idx = content.find('SulaoneTopBar(')
    if idx == -1:
        continue
        
    start_paren = idx + len('SulaoneTopBar')
    end_paren = parse_balanced(content, start_paren)
    if end_paren == -1:
        continue
        
    topappbar_content = content[start_paren+1:end_paren]
    
    # We want to replace `title = "Something", fontWeight = ...` with just `title = "Something"`
    # Or `title = stringResource(...), fontWeight = ...` with just `title = stringResource(...)`
    # Basically we look for `title = ".*?"` or `title = stringResource(.*?)`
    # and remove anything after it until the next newline or `onNavigateBack` or `actions`
    
    new_topappbar_content = re.sub(r'(title\s*=\s*"[^"]*")\s*,[^,\n]*', r'\1', topappbar_content)
    new_topappbar_content = re.sub(r'(title\s*=\s*stringResource\([^)]*\))\s*,[^,\n]*', r'\1', new_topappbar_content)
    
    if topappbar_content != new_topappbar_content:
        new_content = content[:start_paren+1] + new_topappbar_content + content[end_paren:]
        with open(full_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Cleaned up title in {filepath}")
