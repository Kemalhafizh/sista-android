Write-Host "======================================"
Write-Host " SISTA Enterprise - Codebase Health"
Write-Host "======================================"

$rootDir = "c:\project\portofolio\project-super-web\sista-android"
Set-Location $rootDir

# 1. Count total .kt files and LOC
$ktFiles = Get-ChildItem -Path . -Filter *.kt -Recurse
$totalFiles = $ktFiles.Count
$totalLoc = 0
foreach ($file in $ktFiles) {
    $lines = (Get-Content $file.FullName).Count
    $totalLoc += $lines
}
Write-Host "Kotlin Files: $totalFiles"
Write-Host "Total Lines of Code: $totalLoc"

# 2. Detect hardcoded Color(0x...) in ui/ directory
Write-Host "`nChecking for hardcoded colors in UI..."
$hardcodedColors = Get-ChildItem -Path .\app\src\main\java\com\sultanagung1\sista\ui -Filter *.kt -Recurse -ErrorAction SilentlyContinue | Select-String -Pattern "Color\(0x"
if ($hardcodedColors) {
    Write-Host "WARNING: Found hardcoded colors (violates design system):" -ForegroundColor Red
    $hardcodedColors | ForEach-Object { Write-Host "$($_.Path):$($_.LineNumber)" }
} else {
    Write-Host "PASS: No hardcoded colors found in ui directory." -ForegroundColor Green
}

# 3. List TODO/FIXME comments
Write-Host "`nChecking for TODO/FIXME comments..."
$todos = Get-ChildItem -Path . -Filter *.kt -Recurse | Select-String -Pattern "(TODO|FIXME)"
if ($todos) {
    Write-Host "Found TODO/FIXME comments:" -ForegroundColor Yellow
    $todos | ForEach-Object { Write-Host "$($_.Path):$($_.LineNumber) - $($_.Line.Trim())" }
} else {
    Write-Host "PASS: No TODO/FIXME comments found." -ForegroundColor Green
}

# 4. APK Size
$apkPath = ".\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $size = (Get-Item $apkPath).Length / 1MB
    Write-Host "`nDebug APK Size: $([math]::Round($size, 2)) MB"
} else {
    Write-Host "`nDebug APK not found. Run 'gradlew assembleDebug' first."
}

# Output report to file
$reportDir = ".\qa"
if (-not (Test-Path $reportDir)) { New-Item -ItemType Directory -Path $reportDir | Out-Null }
$reportPath = "$reportDir\codebase_report.md"

"## SISTA Codebase Health Report" | Out-File -FilePath $reportPath -Encoding utf8
"**Date**: $(Get-Date)" | Out-File -FilePath $reportPath -Encoding utf8 -Append
"- Total Kotlin Files: $totalFiles" | Out-File -FilePath $reportPath -Encoding utf8 -Append
"- Total Lines of Code: $totalLoc" | Out-File -FilePath $reportPath -Encoding utf8 -Append

Write-Host "`nReport saved to $reportPath"
