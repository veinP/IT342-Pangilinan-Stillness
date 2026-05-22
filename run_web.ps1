# run_web.ps1
$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) { $ScriptDir = Get-Location }
Set-Location $ScriptDir

Write-Host "=========================================" -ForegroundColor Yellow
Write-Host "   StillNess Web Frontend Starter Service" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow

# Load variables from .env if needed
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
}

Write-Host "Navigating to web directory..." -ForegroundColor Cyan
Set-Location "$ScriptDir\web"

# Check if node_modules exists, install if missing
if (-not (Test-Path "node_modules")) {
    Write-Host "node_modules folder not found. Running npm install..." -ForegroundColor Yellow
    npm install
}

Write-Host "Starting Web Dev Server (Vite on port 3000)..." -ForegroundColor Green
npm run dev
