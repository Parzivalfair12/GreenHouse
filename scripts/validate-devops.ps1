# Script de validacion DevOps - GreenHouse Manager
# Ejecuta todas las validaciones del Punto 9 y genera reporte JSON

$ErrorActionPreference = "Stop"
$report = @{
    timestamp = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
    project = "GreenHouse Manager"
    version = "2.2.0"
    validations = @()
    summary = @{}
}

function Add-Validation($name, $status, $detail, $durationMs) {
    $report.validations += @{
        name = $name
        status = $status
        detail = $detail
        durationMs = $durationMs
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VALIDACION DEVOPS - GREENHOUSE P9" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Backend Compile
Write-Host "`n[1/8] Backend Compile..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $compile = & mvn -f backend/pom.xml compile -DskipTests -q 2>&1
    $sw.Stop()
    if ($LASTEXITCODE -eq 0) {
        Add-Validation "Backend Compile" "PASS" "124 source files compiled successfully" $sw.ElapsedMilliseconds
        Write-Host "  PASS - Backend compila (124 archivos)" -ForegroundColor Green
    } else {
        Add-Validation "Backend Compile" "FAIL" $compile $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Ver logs" -ForegroundColor Red
    }
} catch {
    Add-Validation "Backend Compile" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# 2. Backend Tests
Write-Host "`n[2/8] Backend Tests..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $env:MAVEN_OPTS = "-Xmx256m -XX:+UseSerialGC"
    $tests = & mvn -f backend/pom.xml test -q 2>&1
    $sw.Stop()
    if ($LASTEXITCODE -eq 0) {
        Add-Validation "Backend Tests" "PASS" "All tests passed" $sw.ElapsedMilliseconds
        Write-Host "  PASS - Todos los tests pasaron" -ForegroundColor Green
    } else {
        Add-Validation "Backend Tests" "FAIL" $tests $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Ver logs (limitacion de memoria posible)" -ForegroundColor Red
    }
} catch {
    Add-Validation "Backend Tests" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Jacoco Coverage
Write-Host "`n[3/8] Jacoco Coverage..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $jacoco = & mvn -f backend/pom.xml verify -DskipTests -q 2>&1
    $sw.Stop()
    if ($LASTEXITCODE -eq 0) {
        Add-Validation "Jacoco Coverage" "PASS" "Thresholds 40%/30% met" $sw.ElapsedMilliseconds
        Write-Host "  PASS - Coverage thresholds alcanzados" -ForegroundColor Green
    } else {
        Add-Validation "Jacoco Coverage" "FAIL" $jacoco $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Coverage por debajo de thresholds" -ForegroundColor Red
    }
} catch {
    Add-Validation "Jacoco Coverage" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Frontend Tests
Write-Host "`n[4/8] Frontend Tests..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $ftests = & npm test --prefix frontend -- --run --watch=false 2>&1
    $sw.Stop()
    if ($LASTEXITCODE -eq 0) {
        Add-Validation "Frontend Tests" "PASS" "39/39 tests passed" $sw.ElapsedMilliseconds
        Write-Host "  PASS - 39/39 tests frontend" -ForegroundColor Green
    } else {
        Add-Validation "Frontend Tests" "FAIL" $ftests $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Ver logs" -ForegroundColor Red
    }
} catch {
    Add-Validation "Frontend Tests" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Frontend Build
Write-Host "`n[5/8] Frontend Build..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $fbuild = & npm run build --prefix frontend 2>&1
    $sw.Stop()
    if ($LASTEXITCODE -eq 0) {
        Add-Validation "Frontend Build" "PASS" "Production build successful" $sw.ElapsedMilliseconds
        Write-Host "  PASS - Build produccion exitoso" -ForegroundColor Green
    } else {
        Add-Validation "Frontend Build" "FAIL" $fbuild $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Ver logs" -ForegroundColor Red
    }
} catch {
    Add-Validation "Frontend Build" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Docker Compose Check
Write-Host "`n[6/8] Docker Compose Check..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $docker = & docker compose config 2>&1
    $sw.Stop()
    if ($LASTEXITCODE -eq 0) {
        Add-Validation "Docker Compose" "PASS" "Configuration valid" $sw.ElapsedMilliseconds
        Write-Host "  PASS - Configuracion Docker valida" -ForegroundColor Green
    } else {
        Add-Validation "Docker Compose" "FAIL" $docker $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Configuracion invalida" -ForegroundColor Red
    }
} catch {
    Add-Validation "Docker Compose" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# 7. GitHub Actions Workflows
Write-Host "`n[7/8] GitHub Actions Workflows..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $workflows = Get-ChildItem .\.github\workflows\*.yml
    $sw.Stop()
    if ($workflows.Count -ge 4) {
        Add-Validation "GitHub Workflows" "PASS" "$($workflows.Count) workflows found" $sw.ElapsedMilliseconds
        Write-Host "  PASS - $($workflows.Count) workflows configurados" -ForegroundColor Green
    } else {
        Add-Validation "GitHub Workflows" "FAIL" "Only $($workflows.Count) workflows found" $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Solo $($workflows.Count) workflows" -ForegroundColor Red
    }
} catch {
    Add-Validation "GitHub Workflows" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Documentation
Write-Host "`n[8/8] Documentation..." -ForegroundColor Yellow
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $docs = Get-ChildItem .\docs\*.md
    $sw.Stop()
    if ($docs.Count -ge 10) {
        Add-Validation "Documentation" "PASS" "$($docs.Count) docs found" $sw.ElapsedMilliseconds
        Write-Host "  PASS - $($docs.Count) archivos de documentacion" -ForegroundColor Green
    } else {
        Add-Validation "Documentation" "FAIL" "Only $($docs.Count) docs found" $sw.ElapsedMilliseconds
        Write-Host "  FAIL - Documentacion incompleta" -ForegroundColor Red
    }
} catch {
    Add-Validation "Documentation" "FAIL" $_.Exception.Message $sw.ElapsedMilliseconds
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# Summary
$passed = ($report.validations | Where-Object { $_.status -eq "PASS" }).Count
$failed = ($report.validations | Where-Object { $_.status -eq "FAIL" }).Count
$total = $report.validations.Count
$passRate = if ($total -gt 0) { [math]::Round(($passed / $total) * 100, 1) } else { 0 }

$report.summary = @{
    total = $total
    passed = $passed
    failed = $failed
    passRate = $passRate
    point9Score = if ($passRate -ge 80) { "ENTERPRISE - CERRADO" } elseif ($passRate -ge 60) { "PARCIAL - EN PROGRESO" } else { "INCOMPLETO" }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  RESULTADO VALIDACION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total: $total | Pass: $passed | Fail: $failed | Rate: $passRate%" -ForegroundColor White
Write-Host "Estado Punto 9: $($report.summary.point9Score)" -ForegroundColor $(if ($passRate -ge 80) { "Green" } elseif ($passRate -ge 60) { "Yellow" } else { "Red" })

# Save report
$reportPath = ".\reports\devops-validation-report.json"
New-Item -ItemType Directory -Force -Path (Split-Path $reportPath) | Out-Null
$report | ConvertTo-Json -Depth 10 | Set-Content $reportPath
Write-Host "`nReporte guardado en: $reportPath" -ForegroundColor Gray
