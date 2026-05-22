# run_backend.ps1
# Save the current execution directory
$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) { $ScriptDir = Get-Location }
Set-Location $ScriptDir

Write-Host "=========================================" -ForegroundColor Yellow
Write-Host "   StillNess Backend Starter Service" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow

# Load variables from .env
if (Test-Path "$ScriptDir\.env") {
    Write-Host "Loading environment variables from .env..." -ForegroundColor Cyan
    Get-Content "$ScriptDir\.env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $key, $value = $line -split '=', 2
            if ($key -and $value) {
                [System.Environment]::SetEnvironmentVariable($key.Trim(), $value.Trim())
            }
        }
    }
    Write-Host "Environment variables successfully loaded!" -ForegroundColor Green
} else {
    Write-Error "ERROR: .env file not found at the workspace root!"
    Exit 1
}

# Run backend Spring Boot service
Write-Host "Navigating to stillness backend directory..." -ForegroundColor Cyan
Set-Location "$ScriptDir\backend\stillness"

Write-Host "Launching Spring Boot service..." -ForegroundColor Green
.\mvnw.cmd spring-boot:run
