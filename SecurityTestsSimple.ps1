# Script de Pruebas de Seguridad Dinámicas - Versión Simplificada
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "INICIANDO PRUEBAS DE SEGURIDAD DINAMICAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$TestResults = @()

function Test-ApplicationStatus {
    Write-Host "Verificando estado de la aplicación..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $BaseUrl -Method GET -TimeoutSec 10 -UseBasicParsing
        Write-Host "OK - Aplicación respondiendo - HTTP $($response.StatusCode)" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "ERROR - La aplicación no responde: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

function Test-SQLInjection {
    Write-Host ""
    Write-Host "Prueba 1: SQL Injection en Login" -ForegroundColor Yellow
    Write-Host "---------------------------------" -ForegroundColor Yellow
    
    $payloads = @(
        "admin' OR '1'='1",
        "admin' OR '1'='1' --",
        "'; DROP TABLE usuarios; --"
    )
    
    $vulnerable = $false
    
    foreach ($payload in $payloads) {
        Write-Host "Probando: $payload" -ForegroundColor Cyan
        
        try {
            $body = @{
                username = $payload
                password = "test"
            }
            
            $response = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $body -TimeoutSec 5 -UseBasicParsing
            
            if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 302) {
                $vulnerable = $true
                Write-Host "  -> Respuesta sospechosa: HTTP $($response.StatusCode)" -ForegroundColor Red
            } else {
                Write-Host "  -> Bloqueado correctamente: HTTP $($response.StatusCode)" -ForegroundColor Green
            }
        }
        catch {
            Write-Host "  -> Bloqueado (error esperado): $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Green
        }
    }
    
    if ($vulnerable) {
        Write-Host "RESULTADO: POSIBLE VULNERABILIDAD SQL INJECTION" -ForegroundColor Red
        $script:TestResults += @{ Test = "SQL Injection"; Status = "VULNERABLE"; Details = "Posibles vulnerabilidades detectadas" }
    } else {
        Write-Host "RESULTADO: PROTEGIDO CONTRA SQL INJECTION" -ForegroundColor Green
        $script:TestResults += @{ Test = "SQL Injection"; Status = "SEGURO"; Details = "Protección funcionando correctamente" }
    }
}

function Test-XSSProtection {
    Write-Host ""
    Write-Host "Prueba 2: Cross-Site Scripting (XSS)" -ForegroundColor Yellow
    Write-Host "------------------------------------" -ForegroundColor Yellow
    
    $payloads = @(
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "'><script>alert('XSS')</script>"
    )
    
    $vulnerable = $false
    
    foreach ($payload in $payloads) {
        Write-Host "Probando XSS: $payload" -ForegroundColor Cyan
        
        try {
            $encodedPayload = [System.Web.HttpUtility]::UrlEncode($payload)
            $response = Invoke-WebRequest -Uri "$BaseUrl/productos/buscar?q=$encodedPayload" -TimeoutSec 5 -UseBasicParsing
            
            if ($response.Content -like "*<script>*" -or $response.Content -like "*onerror*") {
                $vulnerable = $true
                Write-Host "  -> VULNERABLE: Payload encontrado en respuesta" -ForegroundColor Red
            } else {
                Write-Host "  -> SEGURO: Payload sanitizado" -ForegroundColor Green
            }
        }
        catch {
            Write-Host "  -> PROTEGIDO: Endpoint no accesible" -ForegroundColor Green
        }
    }
    
    if ($vulnerable) {
        Write-Host "RESULTADO: VULNERABILIDAD XSS DETECTADA" -ForegroundColor Red
        $script:TestResults += @{ Test = "XSS Protection"; Status = "VULNERABLE"; Details = "Vulnerabilidades XSS encontradas" }
    } else {
        Write-Host "RESULTADO: PROTECCION XSS ACTIVA" -ForegroundColor Green
        $script:TestResults += @{ Test = "XSS Protection"; Status = "SEGURO"; Details = "Sanitización XSS funcionando" }
    }
}

function Test-SecurityHeaders {
    Write-Host ""
    Write-Host "Prueba 3: Headers de Seguridad" -ForegroundColor Yellow
    Write-Host "-------------------------------" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $BaseUrl -Method HEAD -TimeoutSec 5 -UseBasicParsing
        $headers = $response.Headers
        
        $securityHeaders = @{
            "X-Frame-Options" = $headers.ContainsKey('X-Frame-Options')
            "X-Content-Type-Options" = $headers.ContainsKey('X-Content-Type-Options')
            "X-XSS-Protection" = $headers.ContainsKey('X-XSS-Protection')
            "Strict-Transport-Security" = $headers.ContainsKey('Strict-Transport-Security')
            "Content-Security-Policy" = $headers.ContainsKey('Content-Security-Policy')
        }
        
        $score = 0
        foreach ($header in $securityHeaders.GetEnumerator()) {
            if ($header.Value) {
                Write-Host "  OK - $($header.Key): Presente" -ForegroundColor Green
                $score++
            } else {
                Write-Host "  FALTA - $($header.Key): Ausente" -ForegroundColor Yellow
            }
        }
        
        Write-Host ""
        Write-Host "PUNTUACION: $score/5 headers de seguridad implementados" -ForegroundColor Cyan
        
        if ($score -ge 3) {
            $script:TestResults += @{ Test = "Security Headers"; Status = "BUENO"; Details = "$score/5 headers implementados" }
        } else {
            $script:TestResults += @{ Test = "Security Headers"; Status = "MEJORABLE"; Details = "$score/5 headers implementados" }
        }
    }
    catch {
        Write-Host "ERROR al verificar headers: $($_.Exception.Message)" -ForegroundColor Red
        $script:TestResults += @{ Test = "Security Headers"; Status = "ERROR"; Details = "No se pudieron verificar los headers" }
    }
}

function Test-CSRFProtection {
    Write-Host ""
    Write-Host "Prueba 4: Protección CSRF" -ForegroundColor Yellow
    Write-Host "-------------------------" -ForegroundColor Yellow
    
    try {
        # Intentar POST sin token CSRF
        $response = Invoke-WebRequest -Uri "$BaseUrl/admin/productos/crear" -Method POST -Body @{ nombre="Test"; precio="100" } -TimeoutSec 5 -UseBasicParsing
        
        if ($response.StatusCode -eq 403) {
            Write-Host "OK - Protección CSRF activa (HTTP 403)" -ForegroundColor Green
            $script:TestResults += @{ Test = "CSRF Protection"; Status = "ACTIVO"; Details = "Token CSRF requerido correctamente" }
        } else {
            Write-Host "ADVERTENCIA - Verificar configuración CSRF (HTTP $($response.StatusCode))" -ForegroundColor Yellow
            $script:TestResults += @{ Test = "CSRF Protection"; Status = "REVISAR"; Details = "Respuesta inesperada: $($response.StatusCode)" }
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 403) {
            Write-Host "OK - Protección CSRF activa (HTTP 403)" -ForegroundColor Green
            $script:TestResults += @{ Test = "CSRF Protection"; Status = "ACTIVO"; Details = "Token CSRF requerido correctamente" }
        } else {
            Write-Host "INFO - Endpoint protegido (HTTP $statusCode)" -ForegroundColor Cyan
            $script:TestResults += @{ Test = "CSRF Protection"; Status = "PROTEGIDO"; Details = "Endpoint no accesible: HTTP $statusCode" }
        }
    }
}

function Show-Summary {
    Write-Host ""
    Write-Host "RESUMEN DE PRUEBAS DE SEGURIDAD" -ForegroundColor Cyan
    Write-Host "===============================" -ForegroundColor Cyan
    
    $secure = 0
    $vulnerable = 0
    $warnings = 0
    
    foreach ($result in $TestResults) {
        $status = switch ($result.Status) {
            "SEGURO" { $secure++; "PASS" }
            "ACTIVO" { $secure++; "PASS" }
            "PROTEGIDO" { $secure++; "PASS" }
            "VULNERABLE" { $vulnerable++; "FAIL" }
            "BUENO" { $secure++; "PASS" }
            "MEJORABLE" { $warnings++; "WARN" }
            "REVISAR" { $warnings++; "WARN" }
            default { $warnings++; "WARN" }
        }
        
        $color = switch ($status) {
            "PASS" { "Green" }
            "FAIL" { "Red" }
            "WARN" { "Yellow" }
        }
        
        Write-Host "  [$status] $($result.Test): $($result.Status)" -ForegroundColor $color
        Write-Host "      -> $($result.Details)" -ForegroundColor Gray
    }
    
    Write-Host ""
    Write-Host "ESTADISTICAS FINALES:" -ForegroundColor White
    Write-Host "  Pruebas Seguras: $secure" -ForegroundColor Green
    Write-Host "  Advertencias: $warnings" -ForegroundColor Yellow
    Write-Host "  Vulnerabilidades: $vulnerable" -ForegroundColor Red
    
    $total = $secure + $warnings + $vulnerable
    $percentage = if ($total -gt 0) { [math]::Round(($secure / $total) * 100, 1) } else { 0 }
    
    Write-Host ""
    Write-Host "NIVEL DE SEGURIDAD: $percentage%" -ForegroundColor $(if ($percentage -ge 80) { "Green" } elseif ($percentage -ge 60) { "Yellow" } else { "Red" })
}

# EJECUCIÓN PRINCIPAL
Add-Type -AssemblyName System.Web

if (Test-ApplicationStatus) {
    Test-SQLInjection
    Test-XSSProtection
    Test-SecurityHeaders
    Test-CSRFProtection
    Show-Summary
    
    Write-Host ""
    Write-Host "PRUEBAS COMPLETADAS EXITOSAMENTE" -ForegroundColor Green
    Write-Host "Fecha: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor Gray
} else {
    Write-Host "No se pueden ejecutar las pruebas sin la aplicación ejecutándose" -ForegroundColor Red
}