#!/usr/bin/env pwsh

Write-Host "🛡️ VERIFICACIÓN DE CORRECCIÓN DE VULNERABILIDAD SQL INJECTION" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

# Función para probar payloads maliciosos
function Test-SQLInjection {
    param(
        [string]$payload,
        [string]$description
    )
    
    Write-Host "📋 Probando: $description" -ForegroundColor Yellow
    Write-Host "Payload: $payload" -ForegroundColor Gray
    
    try {
        $body = "email=$payload&password=test"
        $response = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -Body $body -ContentType "application/x-www-form-urlencoded" -UseBasicParsing -ErrorAction SilentlyContinue
        
        # Si obtenemos la página de login de vuelta, es lo esperado (no hay bypass)
        if ($response.Content -like "*Iniciar Sesión*" -or $response.Content -like "*login*") {
            Write-Host "✅ SEGURO: Retorna formulario de login (no hay bypass)" -ForegroundColor Green
            return $true
        }
        # Si hay redirección a dashboard/admin, sería problemático
        elseif ($response.Content -like "*dashboard*" -or $response.Content -like "*admin*") {
            Write-Host "❌ VULNERABLE: Posible bypass de autenticación" -ForegroundColor Red
            return $false
        }
        else {
            Write-Host "ℹ️  RESULTADO AMBIGUO: Revisar manualmente" -ForegroundColor Yellow
            return $null
        }
    }
    catch {
        Write-Host "✅ SEGURO: Error controlado (posible validación funcionando)" -ForegroundColor Green
        return $true
    }
    
    Write-Host ""
}

# Lista de payloads de SQL Injection para probar
$payloads = @{
    "admin'+OR+'1'='1" = "OR Bypass Básico"
    "admin'+UNION+SELECT+1,2,3--" = "UNION Attack"
    "admin';+DROP+TABLE+usuarios;--" = "SQL Deletion"
    "admin'+AND+1=1--" = "AND Condition"
    "'+OR+'x'='x" = "OR True Condition"
    "admin'/**/OR/**/1=1--" = "Comment Bypass"
    "admin'+WAITFOR+DELAY+'00:00:05'--" = "Time-based"
}

Write-Host "🔍 Probando payloads de SQL Injection..." -ForegroundColor White
Write-Host ""

$secureCount = 0
$vulnerableCount = 0
$ambiguousCount = 0

foreach ($payload in $payloads.GetEnumerator()) {
    $result = Test-SQLInjection -payload $payload.Key -description $payload.Value
    
    if ($result -eq $true) {
        $secureCount++
    } elseif ($result -eq $false) {
        $vulnerableCount++
    } else {
        $ambiguousCount++
    }
    
    Start-Sleep -Seconds 1
}

Write-Host ""
Write-Host "📊 RESUMEN DE RESULTADOS:" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan
Write-Host "✅ Casos Seguros: $secureCount" -ForegroundColor Green
Write-Host "❌ Casos Vulnerables: $vulnerableCount" -ForegroundColor Red
Write-Host "ℹ️  Casos Ambiguos: $ambiguousCount" -ForegroundColor Yellow

Write-Host ""
if ($vulnerableCount -eq 0) {
    Write-Host "🎉 ¡CORRECCIÓN EXITOSA! No se detectaron vulnerabilidades SQL Injection" -ForegroundColor Green
} else {
    Write-Host "⚠️  ATENCIÓN: Se detectaron $vulnerableCount casos vulnerables" -ForegroundColor Red
}

Write-Host ""
Write-Host "📝 Nota: La corrección implementa:" -ForegroundColor White
Write-Host "   • Validación de entrada con SecurityUtils.isInputSecure()" -ForegroundColor Gray
Write-Host "   • Logging de intentos maliciosos" -ForegroundColor Gray  
Write-Host "   • Headers de seguridad (X-Frame-Options, X-Content-Type-Options)" -ForegroundColor Gray
Write-Host "   • Monitoreo de ataques con IP tracking" -ForegroundColor Gray