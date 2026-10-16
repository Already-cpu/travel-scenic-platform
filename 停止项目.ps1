$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$pidPath = Join-Path $projectRoot 'runtime\app.pid'
if (-not (Test-Path -LiteralPath $pidPath)) {
    Write-Host 'No running project record was found.' -ForegroundColor Yellow
    exit 0
}
$appPid = [int](Get-Content -LiteralPath $pidPath -Raw)
$process = Get-Process -Id $appPid -ErrorAction SilentlyContinue
if ($process -and $process.ProcessName -eq 'java') {
    Stop-Process -Id $appPid -Force
    Write-Host "Project stopped (PID $appPid)." -ForegroundColor Green
} else {
    Write-Host 'The recorded process no longer exists.' -ForegroundColor Yellow
}
Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
