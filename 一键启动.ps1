$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$runtimeDir = Join-Path $projectRoot 'runtime'
$uploadDir = Join-Path $projectRoot 'uploads'
New-Item -ItemType Directory -Path $runtimeDir -Force | Out-Null
New-Item -ItemType Directory -Path $uploadDir -Force | Out-Null

function Write-Step([string]$message) {
    Write-Host "`n[Travel Website] $message" -ForegroundColor Cyan
}

function Find-Java {
    $command = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    $candidates = @(
        'C:\Program Files\Java\jdk*\bin\java.exe',
        'C:\Program Files\Java\jre*\bin\java.exe',
        'C:\Program Files\Eclipse Adoptium\jdk*\bin\java.exe',
        'C:\Program Files\Microsoft\jdk-*\bin\java.exe'
    )
    foreach ($pattern in $candidates) {
        $match = Get-Item $pattern -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1
        if ($match) { return $match.FullName }
    }
    return $null
}

function Find-MySql {
    $command = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    $match = Get-Item 'C:\Program Files\MySQL\MySQL Server *\bin\mysql.exe' -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending | Select-Object -First 1
    if ($match) { return $match.FullName }
    return $null
}

function Test-Database([string]$mysqlExe, [string]$userName, [string]$password) {
    $oldPassword = $env:MYSQL_PWD
    $oldErrorAction = $ErrorActionPreference
    $env:MYSQL_PWD = $password
    try {
        $ErrorActionPreference = 'SilentlyContinue'
        $null = & $mysqlExe --protocol=TCP --host=127.0.0.1 --port=3306 "--user=$userName" -N -e 'SELECT 1' 2>$null
        $success = $LASTEXITCODE -eq 0
        return $success
    } finally {
        $ErrorActionPreference = $oldErrorAction
        $env:MYSQL_PWD = $oldPassword
    }
}

Write-Step 'Checking Java and MySQL'
$javaExe = Find-Java
if (-not $javaExe) {
    Write-Host 'Java was not found. Install Java 8 or newer, then run the launcher again.' -ForegroundColor Red
    exit 1
}
$mysqlExe = Find-MySql
if (-not $mysqlExe) {
    Write-Host 'MySQL client was not found. Install MySQL 8 or newer, then run the launcher again.' -ForegroundColor Red
    exit 1
}

if (-not (Test-Database $mysqlExe 'root' 'root')) {
    $mysqlService = Get-Service -ErrorAction SilentlyContinue | Where-Object { $_.Name -like 'MySQL*' } | Select-Object -First 1
    if ($mysqlService -and $mysqlService.Status -ne 'Running') {
        Write-Host "Trying to start MySQL service $($mysqlService.Name)..."
        try { Start-Service -Name $mysqlService.Name } catch { }
        Start-Sleep -Seconds 3
    }
}

$dbUser = 'root'
$dbPassword = 'root'
if (-not (Test-Database $mysqlExe $dbUser $dbPassword)) {
    Write-Host 'Cannot connect with the default MySQL account root/root.' -ForegroundColor Yellow
    $enteredUser = Read-Host 'MySQL username (press Enter for root)'
    if ($enteredUser) { $dbUser = $enteredUser }
    $securePassword = Read-Host 'MySQL password' -AsSecureString
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try { $dbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
    if (-not (Test-Database $mysqlExe $dbUser $dbPassword)) {
        Write-Host 'MySQL connection failed. Check the service, port 3306, username, and password.' -ForegroundColor Red
        exit 1
    }
}

$sqlPath = Join-Path $projectRoot 'database\travel_website.sql'
if (-not (Test-Path -LiteralPath $sqlPath)) {
    Write-Host "Database file is missing: $sqlPath" -ForegroundColor Red
    exit 1
}

Write-Step 'Initializing the standalone MySQL database (existing data is preserved)'
$oldPassword = $env:MYSQL_PWD
$env:MYSQL_PWD = $dbPassword
try {
    Get-Content -LiteralPath $sqlPath -Raw -Encoding UTF8 |
        & $mysqlExe --default-character-set=utf8mb4 --protocol=TCP --host=127.0.0.1 --port=3306 "--user=$dbUser"
    if ($LASTEXITCODE -ne 0) { throw 'Database initialization failed.' }

    foreach ($migrationName in @('20261008_feature_upgrade6.sql', '20261008_profile_wallet.sql')) {
        $migrationPath = Join-Path $projectRoot "database\migrations\$migrationName"
        if (Test-Path -LiteralPath $migrationPath) {
            Get-Content -LiteralPath $migrationPath -Raw -Encoding UTF8 |
                & $mysqlExe --default-character-set=utf8mb4 --protocol=TCP --host=127.0.0.1 --port=3306 "--user=$dbUser" travel_website |
                Out-Null
            if ($LASTEXITCODE -ne 0) { throw "Migration failed: $migrationName" }
        }
    }
} finally {
    $env:MYSQL_PWD = $oldPassword
}

$packagedJar = Join-Path $projectRoot 'app\travel-website.jar'
$sourceJar = Join-Path $projectRoot 'backend\target\travel-website-backend-1.0.0.jar'
if (Test-Path -LiteralPath $packagedJar) {
    $jarArgument = 'app\travel-website.jar'
} elseif (Test-Path -LiteralPath $sourceJar) {
    $jarArgument = 'backend\target\travel-website-backend-1.0.0.jar'
} else {
    Write-Host 'Runnable JAR is missing. Make sure the ZIP file was fully extracted.' -ForegroundColor Red
    exit 1
}

try {
    $existing = Invoke-WebRequest -Uri 'http://localhost:8080/api/scenics' -UseBasicParsing -TimeoutSec 2
    if ($existing.StatusCode -eq 200) {
        Write-Host 'The website is already running. Opening it in the browser.' -ForegroundColor Green
        Start-Process 'http://localhost:8080/'
        exit 0
    }
} catch { }

$portOwner = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($portOwner) {
    Write-Host "Port 8080 is occupied by PID $($portOwner.OwningProcess). Close it and try again." -ForegroundColor Red
    exit 1
}

Write-Step 'Starting the website'
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3306/travel_website?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
$env:SPRING_DATASOURCE_USERNAME = $dbUser
$env:SPRING_DATASOURCE_PASSWORD = $dbPassword
$stdoutPath = Join-Path $runtimeDir 'backend.log'
$stderrPath = Join-Path $runtimeDir 'backend-error.log'
$javaArguments = @('-Duser.timezone=Asia/Shanghai', '-jar', $jarArgument)
$process = Start-Process -FilePath $javaExe -ArgumentList $javaArguments -WorkingDirectory $projectRoot `
    -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath -WindowStyle Hidden -PassThru
[IO.File]::WriteAllText((Join-Path $runtimeDir 'app.pid'), [string]$process.Id)

$ready = $false
for ($attempt = 0; $attempt -lt 40; $attempt++) {
    Start-Sleep -Milliseconds 750
    if ($process.HasExited) { break }
    try {
        $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/scenics' -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) { $ready = $true; break }
    } catch { }
}

if (-not $ready) {
    Write-Host 'Startup failed. Recent logs:' -ForegroundColor Red
    if (Test-Path -LiteralPath $stderrPath) { Get-Content -LiteralPath $stderrPath -Tail 30 }
    if (Test-Path -LiteralPath $stdoutPath) { Get-Content -LiteralPath $stdoutPath -Tail 30 }
    exit 1
}

Write-Host "`nStarted successfully." -ForegroundColor Green
Write-Host 'User site: http://localhost:8080/'
Write-Host 'Admin site: http://localhost:8080/admin/login'
Write-Host 'Admin account: admin / admin123'
Write-Host 'Demo user: 13800138000 / 123456'
Start-Process 'http://localhost:8080/'
exit 0
