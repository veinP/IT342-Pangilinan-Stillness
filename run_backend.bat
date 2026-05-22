@echo off
title StillNess Backend Launcher
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run_backend.ps1"
pause
