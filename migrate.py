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

def process_file(filepath):
    full_path = os.path.join(base_path, filepath)
    if not os.path.exists(full_path):
        print(f"File not found: {full_path}")
        return
        
    with open(full_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find TopAppBar
    idx = content.find('TopAppBar(')
    if idx == -1:
        print(f"TopAppBar not found in {filepath}")
        return
        
    start_paren = idx + len('TopAppBar')
    end_paren = parse_balanced(content, start_paren)
    if end_paren == -1:
        print(f"Unbalanced parens in {filepath}")
        return
        
    topappbar_content = content[start_paren+1:end_paren]
    
    # We will try to extract title, onNavigateBack, and actions.
    # We can use regex to extract the parts since they usually are block arguments.
    title_match = re.search(r'title\s*=\s*\{\s*Text\(\s*"([^"]+)"\s*\)\s*\}', topappbar_content)
    if not title_match:
        # Check if it uses stringRes
        title_match = re.search(r'title\s*=\s*\{\s*Text\(\s*stringResource\(id\s*=\s*R\.string\.([^)]+)\)\s*\)\s*\}', topappbar_content)
        if title_match:
            title_str = f'stringResource(id = R.string.{title_match.group(1)})'
        else:
            title_match = re.search(r'title\s*=\s*\{\s*Text\(\s*text\s*=\s*"([^"]+)"\s*\)\s*\}', topappbar_content)
            if title_match:
                title_str = f'"{title_match.group(1)}"'
            else:
                title_match = re.search(r'title\s*=\s*\{\s*Text\(\s*([^)]+)\s*\)\s*\}', topappbar_content)
                if title_match:
                    title_str = title_match.group(1)
                else:
                    title_str = '""'
    else:
        title_str = f'"{title_match.group(1)}"'
        
    nav_match = re.search(r'navigationIcon\s*=\s*\{\s*IconButton\(\s*onClick\s*=\s*([^)]+)\s*\)', topappbar_content)
    if not nav_match:
        nav_match = re.search(r'navigationIcon\s*=\s*\{\s*if.*?IconButton\(\s*onClick\s*=\s*([^)]+)\s*\)', topappbar_content, re.DOTALL)
        
    onNavigateBack_str = nav_match.group(1).strip() if nav_match else None
    if onNavigateBack_str and onNavigateBack_str.endswith('}'):
        # handle onClick = { onNavigateBack() }
        onNavigateBack_str = onNavigateBack_str
        
    actions_match = re.search(r'actions\s*=\s*(\{.*?\})\s*$', topappbar_content, re.DOTALL)
    if not actions_match:
        # maybe actions is in the middle?
        actions_match = re.search(r'actions\s*=\s*(\{(?:[^{}]*|\{(?:[^{}]*|\{[^{}]*\})*\})*\})', topappbar_content, re.DOTALL)
    
    actions_str = actions_match.group(1).strip() if actions_match else None
    
    # Construct SulaoneTopBar
    indent = content[:idx].split('\n')[-1]
    
    new_topappbar = f'SulaoneTopBar(\n{indent}    title = {title_str}'
    if onNavigateBack_str:
        new_topappbar += f',\n{indent}    onNavigateBack = {onNavigateBack_str}'
    if actions_str and actions_str != '{}':
        new_topappbar += f',\n{indent}    actions = {actions_str}'
    new_topappbar += f'\n{indent})'
    
    new_content = content[:idx] + new_topappbar + content[end_paren+1:]
    
    # Remove OptIn if not needed
    if 'TopAppBar' not in new_content and 'ExperimentalMaterial3Api' in new_content:
        # Count usages of things that might need ExperimentalMaterial3Api (like Scaffold? SulaoneTopBar doesn't need it)
        # We can just check if there are other usages of `ExperimentalMaterial3Api`.
        # Usually it's just TopAppBar. If there's DatePicker, ModalBottomSheet etc., we should keep it.
        # Let's remove it if there are no other obvious Material3 experimental usages.
        experimental_components = ['DatePicker', 'TimePicker', 'ModalBottomSheet', 'SearchBar', 'SheetState', 'rememberDatePickerState']
        needs_optin = any(comp in new_content for comp in experimental_components)
        if not needs_optin:
            new_content = re.sub(r'@OptIn\(ExperimentalMaterial3Api::class\)\s*', '', new_content)
            new_content = re.sub(r'import androidx\.compose\.material3\.ExperimentalMaterial3Api\n', '', new_content)
            
    # Remove unused imports
    new_content = re.sub(r'import androidx\.compose\.material3\.TopAppBar\n', '', new_content)
    new_content = re.sub(r'import androidx\.compose\.material3\.TopAppBarDefaults\n', '', new_content)
    if 'Icons.AutoMirrored.Filled.ArrowBack' not in new_content:
        new_content = re.sub(r'import androidx\.compose\.material\.icons\.automirrored\.filled\.ArrowBack\n', '', new_content)
        new_content = re.sub(r'import androidx\.compose\.material\.icons\.Icons\n', '', new_content)
        
    # Add SulaoneTopBar import
    if 'SulaoneTopBar' not in new_content and 'com.sultanagung1.sista.core.designsystem.*' not in new_content:
        # find last import
        last_import_match = list(re.finditer(r'^import .*\n', new_content, re.MULTILINE))
        if last_import_match:
            last_import = last_import_match[-1]
            insert_pos = last_import.end()
            new_content = new_content[:insert_pos] + "import com.sultanagung1.sista.core.designsystem.SulaoneTopBar\n" + new_content[insert_pos:]
            
    with open(full_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
        
    print(f"Updated {filepath}")

for f in files:
    process_file(f)
