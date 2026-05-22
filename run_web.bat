@echo off
title StillNess Web Launcher
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run_web.ps1"
pause
