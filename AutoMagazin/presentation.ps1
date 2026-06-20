param(
    [switch]$NoBuild
)

# ============================================================
# AutoMagazin — Full Presentation Script
# One file: build → start → demo → summary
# ============================================================

$PASS_COLOR = "Green"
$STEP_COLOR = "Cyan"
$CMD_COLOR = "Yellow"
$ERR_COLOR = "Red"
$HEADER_COLOR = "Magenta"

$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
$ROOT = "D:\MyProjects\AutoMagazin\AutoMagazin"

function Title($text) {
    Write-Host "`n==============================================" -ForegroundColor $HEADER_COLOR
    Write-Host " $text" -ForegroundColor $STEP_COLOR
    Write-Host "==============================================" -ForegroundColor $HEADER_COLOR
}

# ─── 1. BUILD ───────────────────────────────────────────────
Title "STEP 1: BUILD & TEST"

if (-not $NoBuild) {
    Write-Host "Compiling project and running tests..." -ForegroundColor $CMD_COLOR
    Write-Host "Command: .\mvnw.cmd clean test" -ForegroundColor Gray
    Write-Host ""

    $buildResult = cmd /c "cd /d $ROOT && .\mvnw.cmd clean test 2>&1"
    $buildOutput = $buildResult -join "`n"

    if ($buildOutput -match "BUILD SUCCESS") {
        Write-Host "`nBUILD SUCCESS" -ForegroundColor $PASS_COLOR
    } else {
        Write-Host "`nBUILD FAILED" -ForegroundColor $ERR_COLOR
        Write-Host $buildOutput -ForegroundColor $ERR_COLOR
        Write-Host "`nPress Enter to continue anyway (or Ctrl+C to abort)..." -ForegroundColor Gray
        Read-Host
    }
} else {
    Write-Host "Skipping build (-NoBuild flag)" -ForegroundColor Gray
}

# ─── 2. CLEAN DB ────────────────────────────────────────────
Title "STEP 2: CLEAN DATABASE"

Write-Host "Removing old H2 database..." -ForegroundColor $CMD_COLOR
Remove-Item -LiteralPath "$ROOT\data" -Recurse -Force -ErrorAction SilentlyContinue
if (Test-Path "$ROOT\data") {
    Write-Host "FAILED to clean database" -ForegroundColor $ERR_COLOR
} else {
    Write-Host "Database cleaned. Demo users will be re-created on startup." -ForegroundColor $PASS_COLOR
}

Write-Host "`nPress Enter to start the application..." -ForegroundColor Gray
Read-Host

# ─── 3. START APP ───────────────────────────────────────────
Title "STEP 3: START APPLICATION"

Write-Host "Starting application with dev profile..." -ForegroundColor $CMD_COLOR
Write-Host "Command: .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor Gray
Write-Host ""

$appProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d $ROOT && .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev" -PassThru -WindowStyle Minimized

Write-Host "Waiting for startup" -ForegroundColor Gray -NoNewline

$appReady = $false
for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 1
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"x","password":"x"}' -UseBasicParsing -ErrorAction SilentlyContinue
        $appReady = $true
        break
    } catch {
        if ($_.Exception.Response) {
            $appReady = $true
            break
        }
    }
    if ($i % 5 -eq 4) { Write-Host "." -ForegroundColor Gray -NoNewline }
}

if ($appReady) {
    Write-Host " ready!" -ForegroundColor $PASS_COLOR
    Write-Host "Application is running at http://localhost:8080" -ForegroundColor $PASS_COLOR
} else {
    Write-Host " FAILED!" -ForegroundColor $ERR_COLOR
    Write-Host "Could not start the application. Check logs." -ForegroundColor $ERR_COLOR
    Write-Host "Press Enter to exit..." -ForegroundColor Gray
    Read-Host
    exit 1
}

Write-Host "`nPress Enter to start the demo..." -ForegroundColor Gray
Read-Host

# ─── 4. RUN DEMO ────────────────────────────────────────────
Title "STEP 4: DEMONSTRATION"

Write-Host "Running demo scenario..." -ForegroundColor $CMD_COLOR
Write-Host "All 14 steps will run with pauses between each." -ForegroundColor Gray
Write-Host ""

# Run demo.ps1 — it will handle all steps with PauseStep inside
& "$ROOT\demo.ps1"

# ─── 5. SUMMARY ─────────────────────────────────────────────
Title "PRESENTATION COMPLETE"

Write-Host @"
Demo users:
  Admin: admin / admin123
  User:  user / user123

Useful links:
  Swagger UI:   http://localhost:8080/swagger-ui/index.html
  OpenAPI JSON: http://localhost:8080/v3/api-docs
  H2 Console:   http://localhost:8080/h2-console
      JDBC: jdbc:h2:file:./data/devdb
      User: sa
      Password: (empty)

The application is still running.
Close this window or stop Java process manually when done.

"@ -ForegroundColor $HEADER_COLOR

Write-Host "Stop the application? (Y/N, default N): " -ForegroundColor Gray -NoNewline
$answer = Read-Host
if ($answer -eq "Y" -or $answer -eq "y") {
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "Application stopped." -ForegroundColor $PASS_COLOR
}
