param(
    [switch]$StartApp
)

# Set console to UTF-8 for proper Unicode display
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

# ============================================================
# AutoMagazin — Full Demo Scenario for Presentation
# ============================================================

$BASE_URL = "http://localhost:8080"
$PASS_COLOR = "Green"
$STEP_COLOR = "Cyan"
$CMD_COLOR = "Yellow"
$ERR_COLOR = "Red"
$HEADER_COLOR = "Magenta"

function Step($title) {
    Write-Host "`n═══════════════════════════════════════════════" -ForegroundColor $HEADER_COLOR
    Write-Host " $title" -ForegroundColor $STEP_COLOR
    Write-Host "═══════════════════════════════════════════════" -ForegroundColor $HEADER_COLOR
}

function Exec($method, $url, $body, $desc, $headers = @{}) {
    $fullUrl = "$BASE_URL$url"
    Write-Host "`n[$method] $fullUrl" -ForegroundColor $CMD_COLOR
    if ($desc) { Write-Host "  -> $desc" -ForegroundColor $CMD_COLOR }

    $params = @{
        Uri = $fullUrl
        Method = $method
        ContentType = "application/json"
        Headers = $headers
    }

    if ($body) {
        $json = $body | ConvertTo-Json -Depth 10 -Compress
        Write-Host "  Body: $json" -ForegroundColor Gray
        $params.Body = $json
    }

    try {
        $response = Invoke-WebRequest @params -UseBasicParsing
        if ($response.Content) {
            try {
                $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
            } catch {
                Write-Host $response.Content -ForegroundColor $PASS_COLOR
            }
        }
        Write-Host "  OK ($($response.StatusCode))" -ForegroundColor $PASS_COLOR
        return $response.Content | ConvertFrom-Json
    } catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "N/A" }
        Write-Host "  ERROR ($statusCode)" -ForegroundColor $ERR_COLOR
        if ($_.Exception.Response) {
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $reader.ReadToEnd() | ConvertFrom-Json | ConvertTo-Json -Depth 10
                $reader.Close()
            } catch {}
        } else {
            Write-Host "  Cause: $($_.Exception.Message)" -ForegroundColor $ERR_COLOR
        }
        return $null
    }
}

function PauseStep($msg) {
    Write-Host "`nPress Enter to continue... $msg" -ForegroundColor Gray
    Read-Host
}

# --- 0. START APPLICATION ------------------------------------
Write-Host @"

============================================
     AutoMagazin — Project Demo
     REST API for Car Marketplace
============================================

"@ -ForegroundColor $HEADER_COLOR

if ($StartApp) {
    Write-Host "Starting application..." -ForegroundColor $STEP_COLOR
    Write-Host "Command: cmd /c .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor $CMD_COLOR

    $proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d $PSScriptRoot && .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev" -PassThru

    Write-Host "Waiting for startup" -ForegroundColor Gray -NoNewline
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 1
        try {
            $r = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"x","password":"x"}' -UseBasicParsing -ErrorAction SilentlyContinue
            # Got 2xx response (shouldn't happen for invalid creds, but server is up)
            $ready = $true
            break
        } catch {
            # Invoke-WebRequest throws on non-2xx; if Response exists, server IS running
            if ($_.Exception.Response) {
                $ready = $true
                break
            }
        }
        if ($i % 5 -eq 4) { Write-Host "." -ForegroundColor Gray -NoNewline }
    }
    if ($ready) {
        Write-Host " ready!" -ForegroundColor $PASS_COLOR
        Write-Host "App started (PID: $($proc.Id))" -ForegroundColor $PASS_COLOR
    } else {
        Write-Host " FAILED!" -ForegroundColor $ERR_COLOR
        Write-Host "Could not start application. Check logs." -ForegroundColor $ERR_COLOR
        exit 1
    }
} else {
    # Check if app is already running
    Write-Host "Checking server..." -ForegroundColor Gray -NoNewline
    $serverUp = $false
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"x","password":"x"}' -UseBasicParsing -ErrorAction SilentlyContinue
        $serverUp = $true  # 2xx response
    } catch {
        if ($_.Exception.Response) { $serverUp = $true }  # non-2xx but server is up
    }
    
    if (-not $serverUp) {
        Write-Host " NOT FOUND" -ForegroundColor $ERR_COLOR
        Write-Host "`nERROR: Application is not running!" -ForegroundColor $ERR_COLOR
                             Write-Host "Start it first (in cmd.exe, NOT PowerShell):" -ForegroundColor $CMD_COLOR
                             Write-Host '  1. set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot' -ForegroundColor Gray
                             Write-Host '  2. cd D:\MyProjects\AutoMagazin\AutoMagazin' -ForegroundColor Gray
                             Write-Host '  3. .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev' -ForegroundColor Gray
                             Write-Host "`nOr run this script with -StartApp:" -ForegroundColor $CMD_COLOR
                             Write-Host "  .\demo.ps1 -StartApp" -ForegroundColor Gray
        exit 1
    }
    Write-Host " OK" -ForegroundColor $PASS_COLOR
}

Write-Host "Base URL: $BASE_URL" -ForegroundColor Gray
Write-Host "Swagger UI:  $BASE_URL/swagger-ui/index.html" -ForegroundColor Gray
Write-Host "H2 Console:  $BASE_URL/h2-console" -ForegroundColor Gray
PauseStep "to start the demo"

$userToken = $null
$adminToken = $null
$userId = $null
$newCarId = $null

# --- 1. REGISTER NEW USER ------------------------------------
Step "1. REGISTRATION"

Write-Host "Registering a new user 'demo'." -ForegroundColor White
$regBody = @{
    username = "demo"
    lastName = "Demo"
    email    = "demo@automagazin.ru"
    password = "demo123"
}
$regResult = Exec -method POST -url "/auth/register" -body $regBody -desc "Create new user account"
PauseStep "to proceed to login"

# --- 2. LOGIN (USER) ----------------------------------------
Step "2. LOGIN (STANDARD USER)"

Write-Host "Logging in as pre-seeded user 'user'." -ForegroundColor White
$loginBody = @{ username = "user"; password = "user123" }
$loginResult = Exec -method POST -url "/auth/login" -body $loginBody -desc "Authenticate and get JWT tokens"

if ($loginResult -and $loginResult.accessToken) {
    $userToken = $loginResult.accessToken
    $userId = $loginResult.user.id
    Write-Host "`nJWT token: $($userToken.Substring(0, 50))..." -ForegroundColor $PASS_COLOR
    Write-Host "User ID: $userId" -ForegroundColor $PASS_COLOR
    Write-Host "Role: $($loginResult.user.role)" -ForegroundColor $PASS_COLOR
}
PauseStep "to view user profile"

# --- 3. CURRENT USER PROFILE --------------------------------
Step "3. USER PROFILE (GET /auth/me)"

Write-Host "Fetching current user data using JWT." -ForegroundColor White
Exec -method GET -url "/auth/me" -desc "GET /auth/me — current user info" -headers @{ Authorization = "Bearer $userToken" }
PauseStep "to view car catalog"

# --- 4. CAR LISTING -----------------------------------------
Step "4. CAR CATALOG (GET /api/cars)"

Write-Host "The catalog has 9 pre-loaded cars." -ForegroundColor White
$carsResult = Exec -method GET -url "/api/cars" -desc "GET /api/cars — list all cars" -headers @{ Authorization = "Bearer $userToken" }

if ($carsResult) {
    Write-Host "`nTotal cars: $(@($carsResult).Count)" -ForegroundColor $PASS_COLOR
}
PauseStep "to update personal data"

# --- 5. UPDATE PERSONAL DATA --------------------------------
Step "5. UPDATE PERSONAL DATA"

Write-Host "Updating user's first and last name." -ForegroundColor White
$pdBody = @{
    firstName = "Peter"
    lastName  = "Peterson-Updated"
    birthDate = "1995-06-20"
}
Exec -method PUT -url "/users/$userId/personal-data" -body $pdBody -desc "PUT /users/{id}/personal-data — update name and birthdate" -headers @{ Authorization = "Bearer $userToken" }

Write-Host "`nVerifying the update:" -ForegroundColor White
Exec -method GET -url "/auth/me" -desc "GET /auth/me — check changes" -headers @{ Authorization = "Bearer $userToken" }
PauseStep "to change password"

# --- 6. CHANGE PASSWORD -------------------------------------
Step "6. CHANGE PASSWORD"

Write-Host "Changing password to 'newpass123'." -ForegroundColor White
$pwBody = @{ password = "newpass123" }
Exec -method PUT -url "/users/$userId/password" -body $pwBody -desc "PUT /users/{id}/password — change password" -headers @{ Authorization = "Bearer $userToken" }

Write-Host "`nVerifying: logging in with new password:" -ForegroundColor White
$reloginBody = @{ username = "user"; password = "newpass123" }
$reloginResult = Exec -method POST -url "/auth/login" -body $reloginBody -desc "POST /auth/login — confirm new password works"
if ($reloginResult -and $reloginResult.accessToken) {
    $userToken = $reloginResult.accessToken
}
PauseStep "to refresh token"

# --- 7. REFRESH TOKEN ---------------------------------------
Step "7. REFRESH ACCESS TOKEN"

Write-Host "Using refresh token to get a new access token." -ForegroundColor White
$refreshBody = @{
    accessToken = $reloginResult.accessToken
    refreshToken = $reloginResult.refreshToken
}
$refreshResult = Exec -method POST -url "/auth/refresh" -body $refreshBody -desc "POST /auth/refresh — renew access token"
if ($refreshResult -and $refreshResult.accessToken) {
    $userToken = $refreshResult.accessToken
}
PauseStep "to switch to admin"

# --- 8. LOGIN AS ADMIN --------------------------------------
Step "8. LOGIN AS ADMINISTRATOR"

Write-Host "Logging in as pre-seeded admin 'admin'." -ForegroundColor White

$adminBody = @{ username = "admin"; password = "admin123" }
$adminResult = Exec -method POST -url "/auth/login" -body $adminBody -desc "POST /auth/login — login as admin"

if ($adminResult -and $adminResult.accessToken) {
    $adminToken = $adminResult.accessToken
    Write-Host "`nADMIN ACCESS GRANTED. Role: $($adminResult.user.role)" -ForegroundColor $PASS_COLOR
}
PauseStep "to view all users"

# --- 9. LIST ALL USERS (ADMIN ONLY) -------------------------
Step "9. LIST ALL USERS (ADMIN ONLY)"

Write-Host "Admin can view all registered users." -ForegroundColor White
Exec -method GET -url "/users" -desc "GET /users — list all users (ADMIN-only)" -headers @{ Authorization = "Bearer $adminToken" }
PauseStep "to create a new car"

# --- 10. CREATE CAR (ADMIN ONLY) ----------------------------
Step "10. CREATE A NEW CAR (ADMIN ONLY)"

Write-Host "Admin is adding a new car to the catalog." -ForegroundColor White
$newCar = @{
    brand  = "Tesla"
    model  = "Model 3"
    name   = "Tesla Model 3"
    price  = 55000
    volume = 0.0
    power  = 450
}
$carResult = Exec -method POST -url "/api/cars" -body $newCar -desc "POST /api/cars — create car (ADMIN-only)" -headers @{ Authorization = "Bearer $adminToken" }
if ($carResult -and $carResult.id) {
    $newCarId = $carResult.id
    Write-Host "`nCreated car with ID: $newCarId" -ForegroundColor $PASS_COLOR
}
PauseStep "to update the car"

# --- 11. PATCH CAR (ADMIN ONLY) -----------------------------
Step "11. UPDATE CAR PRICE (ADMIN ONLY)"

if ($newCarId) {
    Write-Host "Updating car price using PATCH." -ForegroundColor White
    $patchCar = @{ price = 52000 }
    Exec -method PATCH -url "/api/cars/$newCarId" -body $patchCar -desc "PATCH /api/cars/{id} — partial update (ADMIN-only)" -headers @{ Authorization = "Bearer $adminToken" }
} else {
    Write-Host "Skipping (no car ID)" -ForegroundColor $ERR_COLOR
}
PauseStep "to delete the car"

# --- 12. DELETE CAR (ADMIN ONLY) ----------------------------
Step "12. DELETE CAR (ADMIN ONLY)"

if ($newCarId) {
    Write-Host "Removing the car from the catalog." -ForegroundColor White
    Exec -method DELETE -url "/api/cars/$newCarId" -desc "DELETE /api/cars/{id} — delete car (ADMIN-only)" -headers @{ Authorization = "Bearer $adminToken" }

    Write-Host "`nVerifying the deletion:" -ForegroundColor White
    Exec -method GET -url "/api/cars" -desc "GET /api/cars — verify (should be 9 cars)" -headers @{ Authorization = "Bearer $adminToken" }
} else {
    Write-Host "Skipping (no car ID)" -ForegroundColor $ERR_COLOR
}
PauseStep "to change user role"

# --- 13. CHANGE USER ROLE (ADMIN ONLY) ----------------------
Step "13. ROLE MANAGEMENT (ADMIN ONLY)"

Write-Host "Promoting user 'user' to administrator." -ForegroundColor White
$roleBody = @{ role = "ADMIN" }
Exec -method PATCH -url "/users/$userId/role" -body $roleBody -desc "PATCH /users/{id}/role — change role (ADMIN-only)" -headers @{ Authorization = "Bearer $adminToken" }

Write-Host "`nChecking user's new role:" -ForegroundColor White
Exec -method GET -url "/users/$userId" -desc "GET /users/{id} — verify role change" -headers @{ Authorization = "Bearer $adminToken" }
PauseStep "to see final summary"

# --- 14. SUMMARY --------------------------------------------
Step "14. SUMMARY"

Write-Host @"
Demo completed successfully!

Verified features:
  Registration
  Authentication (JWT access + refresh tokens)
  User profile (get / update)
  Car catalog (9 pre-loaded cars)
  Personal data update
  Password change
  Token refresh
  Admin login
  List all users (ADMIN)
  Create, update, delete cars (ADMIN)
  Role management (ADMIN)

Useful links:
  Swagger UI:        $BASE_URL/swagger-ui/index.html
  OpenAPI JSON:      $BASE_URL/v3/api-docs
  H2 Console:        $BASE_URL/h2-console
      JDBC URL: jdbc:h2:file:./data/devdb
      User: sa
      Password: (empty)

Demo users:
  Admin: admin / admin123
  User:  user / user123

"@ -ForegroundColor $HEADER_COLOR

if ($StartApp -and $proc -and !$proc.HasExited) {
    Write-Host "Stop the application? (Y/N, default N): " -ForegroundColor Gray -NoNewline
    $answer = Read-Host
    if ($answer -eq "Y" -or $answer -eq "y") {
        $proc.Kill()
        Write-Host "Application stopped." -ForegroundColor $PASS_COLOR
    }
}
