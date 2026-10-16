@echo off
chcp 65001 >nul
cd /d "%~dp0"
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0一键启动.ps1"
if errorlevel 1 (
  echo.
  echo 启动失败，请查看上方提示或 runtime\backend-error.log。
  pause
)
