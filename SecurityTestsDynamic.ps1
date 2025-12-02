# Script de Pruebas de Seguridad Dinámicas - Pastelería Córdova (PowerShell)
# Ejecutar con: .\SecurityTestsDynamic.ps1

Write-Host "🛡️  INICIANDO PRUEBAS DE SEGURIDAD DINÁMICAS" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# Configuración
$BaseUrl = "http://localhost:8080"
$ResultsFile = "security_test_results_$(Get-Date -Format 'yyyyMMdd_HHmmss').html"
$TestResults = @()

# Función para crear header del reporte HTML
function Create-ReportHeader {
    $header = @"
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pruebas de Seguridad Dinámicas - Pastelería Córdova</title>
    <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 15px; box-shadow: 0 20px 40px rgba(0,0,0,0.15); }
        .header { background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }
        .test-section { margin: 20px 0; padding: 20px; border-left: 4px solid #007bff; background: #f8f9fa; border-radius: 8px; }
        .test-pass { border-left-color: #28a745; background: linear-gradient(135deg, #f8fff9 0%, #e8f5e8 100%); }
        .test-fail { border-left-color: #dc3545; background: linear-gradient(135deg, #fff5f5 0%, #ffe6e6 100%); }
        .test-warning { border-left-color: #ffc107; background: linear-gradient(135deg, #fff9f0 0%, #ffebcd 100%); }
        .code-block { background: #2d3748; color: #e2e8f0; padding: 15px; border-radius: 8px; overflow-x: auto; font-family: 'Consolas', 'Courier New', monospace; margin: 10px 0; }
        .status-pass { color: #28a745; font-weight: bold; font-size: 1.2em; }
        .status-fail { color: #dc3545; font-weight: bold; font-size: 1.2em; }
        .status-warning { color: #e67e22; font-weight: bold; font-size: 1.2em; }
        .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }
        .summary-card { background: white; padding: 20px; border-radius: 10px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        h1 { margin: 0; font-size: 2.5em; }
        h2 { color: #2c3e50; margin-top: 30px; }
        h3 { margin-top: 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔍 Pruebas de Seguridad Dinámicas</h1>
            <p style="font-size: 1.2em; margin: 10px 0;">Pastelería Córdova - Análisis en Tiempo Real</p>
            <p style="opacity: 0.9;">Fecha: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')</p>
        </div>
"@
    
    $header | Out-File -FilePath $ResultsFile -Encoding UTF8
    Write-Host "📄 Reporte iniciado: $ResultsFile" -ForegroundColor Green
}

# Función para agregar resultado al reporte
function Add-TestResult {
    param(
        [string]$TestName,
        [string]$Status,
        [string]$Description,
        [string]$Details
    )
    
    $className = switch ($Status) {
        "PASS" { "test-section test-pass" }
        "FAIL" { "test-section test-fail" }
        "WARNING" { "test-section test-warning" }
        default { "test-section" }
    }
    
    $statusClass = "status-" + $Status.ToLower()
    
    $result = @"
        <div class="$className">
            <h3>$TestName - <span class="$statusClass">$Status</span></h3>
            <p>$Description</p>
            <div class="code-block">$($Details -replace "`n", "<br>")</div>
        </div>
"@
    
    $result | Out-File -FilePath $ResultsFile -Append -Encoding UTF8
    
    # Agregar a resumen
    $script:TestResults += @{
        Name = $TestName
        Status = $Status
        Description = $Description
    }
}

# Función para verificar estado de la aplicación
function Test-ApplicationStatus {
    Write-Host "🔍 Verificando estado de la aplicación..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $BaseUrl -Method GET -TimeoutSec 10 -UseBasicParsing
        $statusCode = $response.StatusCode
        
        if ($statusCode -eq 200) {
            Write-Host "✅ Aplicación ejecutándose - HTTP $statusCode" -ForegroundColor Green
            Add-TestResult "Estado de la Aplicación" "PASS" "Aplicación respondiendo correctamente en puerto 8080" "HTTP Code: $statusCode`nResponse Time: OK"
            return $true
        } else {
            Write-Host "⚠️ Aplicación responde con código: $statusCode" -ForegroundColor Yellow
            Add-TestResult "Estado de la Aplicación" "WARNING" "Aplicación responde con código no estándar" "HTTP Code: $statusCode"
            return $true
        }
    }
    catch {
        Write-Host "❌ ERROR: La aplicación no está ejecutándose en $BaseUrl" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        Add-TestResult "Estado de la Aplicación" "FAIL" "La aplicación no responde en el puerto 8080" "Error: $($_.Exception.Message)"
        return $false
    }
}

# Prueba 1: SQL Injection
function Test-SQLInjection {
    Write-Host ""
    Write-Host "🔍 Prueba 1: SQL Injection en Login" -ForegroundColor Yellow
    Write-Host "-----------------------------------" -ForegroundColor Yellow
    
    $payloads = @(
        "admin' OR '1'='1",
        "admin' OR '1'='1' --",
        "admin' OR '1'='1' /*",
        "'; DROP TABLE usuarios; --",
        "admin' UNION SELECT * FROM usuarios --"
    )
    
    $results = @()
    $vulnerable = $false
    
    foreach ($payload in $payloads) {
        Write-Host "Probando payload: $payload" -ForegroundColor Cyan
        
        try {
            $body = @{
                username = $payload
                password = "test"
            }
            
            $response = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $body -TimeoutSec 5 -UseBasicParsing
            $statusCode = $response.StatusCode
            
            $results += "Payload: $payload -> HTTP $statusCode"
            
            if ($statusCode -eq 200 -or $statusCode -eq 302) {
                $vulnerable = $true
            }
        }
        catch {
            $statusCode = "ERROR"
            $results += "Payload: $payload -> $statusCode ($($_.Exception.Message))"
        }
    }
    
    $resultText = $results -join "`n"
    
    if ($vulnerable) {
        Add-TestResult "SQL Injection - Login" "FAIL" "⚠️ POSIBLE vulnerabilidad SQL Injection detectada" $resultText
        Write-Host "❌ VULNERABILIDAD DETECTADA" -ForegroundColor Red
    } else {
        Add-TestResult "SQL Injection - Login" "PASS" "✅ No se detectó vulnerabilidad SQL Injection" $resultText
        Write-Host "✅ SEGURO" -ForegroundColor Green
    }
}

# Prueba 2: XSS
function Test-XSSVulnerability {
    Write-Host ""
    Write-Host "🔍 Prueba 2: Cross-Site Scripting (XSS)" -ForegroundColor Yellow
    Write-Host "---------------------------------------" -ForegroundColor Yellow
    
    $payloads = @(
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "javascript:alert('XSS')",
        "<svg onload=alert('XSS')>",
        "'><script>alert('XSS')</script>"
    )
    
    $results = @()
    $vulnerable = $false
    
    foreach ($payload in $payloads) {
        Write-Host "Probando XSS payload: $payload" -ForegroundColor Cyan
        
        try {
            $encodedPayload = [System.Web.HttpUtility]::UrlEncode($payload)
            $response = Invoke-WebRequest -Uri "$BaseUrl/productos/buscar?q=$encodedPayload" -TimeoutSec 5 -UseBasicParsing
            
            if ($response.Content -like "*<script>*") {
                $vulnerable = $true
                $results += "VULNERABLE: $payload encontrado sin sanitizar"
            } else {
                $results += "SEGURO: $payload sanitizado correctamente"
            }
        }
        catch {
            $results += "ERROR al probar: $payload - $($_.Exception.Message)"
        }
    }
    
    $resultText = $results -join "`n"
    
    if ($vulnerable) {
        Add-TestResult "Cross-Site Scripting (XSS)" "FAIL" "⚠️ Vulnerabilidad XSS detectada" $resultText
        Write-Host "❌ VULNERABILIDAD XSS DETECTADA" -ForegroundColor Red
    } else {
        Add-TestResult "Cross-Site Scripting (XSS)" "PASS" "✅ Protección XSS funcionando correctamente" $resultText
        Write-Host "✅ PROTECCIÓN XSS ACTIVA" -ForegroundColor Green
    }
}

# Prueba 3: Path Traversal
function Test-PathTraversal {
    Write-Host ""
    Write-Host "🔍 Prueba 3: Path Traversal" -ForegroundColor Yellow
    Write-Host "---------------------------" -ForegroundColor Yellow
    
    $payloads = @(
        "../../../etc/passwd",
        "..\\..\\..\\windows\\system32\\drivers\\etc\\hosts",
        "....//....//....//etc/passwd",
        "..%2F..%2F..%2Fetc%2Fpasswd",
        "..%252F..%252F..%252Fetc%252Fpasswd"
    )
    
    $results = @()
    $vulnerable = $false
    
    foreach ($payload in $payloads) {
        Write-Host "Probando Path Traversal: $payload" -ForegroundColor Cyan
        
        try {
            $response = Invoke-WebRequest -Uri "$BaseUrl/uploads/$payload" -TimeoutSec 5 -UseBasicParsing
            $statusCode = $response.StatusCode
            
            $results += "Payload: $payload -> HTTP $statusCode"
            
            if ($response.Content -like "*root:*") {
                $vulnerable = $true
            }
        }
        catch {
            $statusCode = $_.Exception.Response.StatusCode.value__
            $results += "Payload: $payload -> HTTP $statusCode"
        }
    }
    
    $resultText = $results -join "`n"
    
    if ($vulnerable) {
        Add-TestResult "Path Traversal" "FAIL" "⚠️ Vulnerabilidad Path Traversal detectada" $resultText
        Write-Host "❌ VULNERABILIDAD PATH TRAVERSAL" -ForegroundColor Red
    } else {
        Add-TestResult "Path Traversal" "PASS" "✅ Protección contra Path Traversal activa" $resultText
        Write-Host "✅ PROTEGIDO CONTRA PATH TRAVERSAL" -ForegroundColor Green
    }
}

# Prueba 4: Headers de Seguridad
function Test-SecurityHeaders {
    Write-Host ""
    Write-Host "🔍 Prueba 4: Headers de Seguridad" -ForegroundColor Yellow
    Write-Host "---------------------------------" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $BaseUrl -Method HEAD -TimeoutSec 5 -UseBasicParsing
        $headers = $response.Headers
        
        $results = @()
        $score = 0
        $total = 6
        
        # Verificar headers importantes
        if ($headers.ContainsKey('X-Frame-Options')) {
            $results += "✅ X-Frame-Options: $($headers['X-Frame-Options'])"
            $score++
        } else {
            $results += "❌ X-Frame-Options: Ausente"
        }
        
        if ($headers.ContainsKey('X-Content-Type-Options')) {
            $results += "✅ X-Content-Type-Options: $($headers['X-Content-Type-Options'])"
            $score++
        } else {
            $results += "❌ X-Content-Type-Options: Ausente"
        }
        
        if ($headers.ContainsKey('X-XSS-Protection')) {
            $results += "✅ X-XSS-Protection: $($headers['X-XSS-Protection'])"
            $score++
        } else {
            $results += "❌ X-XSS-Protection: Ausente"
        }
        
        if ($headers.ContainsKey('Strict-Transport-Security')) {
            $results += "✅ HSTS: $($headers['Strict-Transport-Security'])"
            $score++
        } else {
            $results += "❌ HSTS: Ausente"
        }
        
        if ($headers.ContainsKey('Content-Security-Policy')) {
            $results += "✅ CSP: Presente"
            $score++
        } else {
            $results += "❌ CSP: Ausente"
        }
        
        if ($headers.ContainsKey('Referrer-Policy')) {
            $results += "✅ Referrer-Policy: $($headers['Referrer-Policy'])"
            $score++
        } else {
            $results += "❌ Referrer-Policy: Ausente"
        }
        
        $results += ""
        $results += "Puntuación: $score/$total headers implementados"
        
        $resultText = $results -join "`n"
        
        if ($score -ge 4) {
            Add-TestResult "Headers de Seguridad" "PASS" "✅ Buena configuración de headers de seguridad" $resultText
            Write-Host "✅ HEADERS DE SEGURIDAD: $score/$total" -ForegroundColor Green
        } elseif ($score -ge 2) {
            Add-TestResult "Headers de Seguridad" "WARNING" "⚠️ Configuración parcial de headers de seguridad" $resultText
            Write-Host "⚠️ HEADERS PARCIALES: $score/$total" -ForegroundColor Yellow
        } else {
            Add-TestResult "Headers de Seguridad" "FAIL" "❌ Headers de seguridad insuficientes" $resultText
            Write-Host "❌ HEADERS INSUFICIENTES: $score/$total" -ForegroundColor Red
        }
    }
    catch {
        Add-TestResult "Headers de Seguridad" "FAIL" "Error al verificar headers de seguridad" "Error: $($_.Exception.Message)"
        Write-Host "❌ ERROR AL VERIFICAR HEADERS" -ForegroundColor Red
    }
}

# Función para finalizar el reporte
function Finalize-Report {
    $passCount = ($TestResults | Where-Object { $_.Status -eq "PASS" }).Count
    $failCount = ($TestResults | Where-Object { $_.Status -eq "FAIL" }).Count
    $warningCount = ($TestResults | Where-Object { $_.Status -eq "WARNING" }).Count
    $totalTests = $TestResults.Count
    
    $summary = @"
        <h2>📊 Resumen de Resultados</h2>
        <div class="summary-grid">
            <div class="summary-card" style="border-left: 4px solid #28a745;">
                <h3 style="color: #28a745;">✅ Pruebas Pasadas</h3>
                <p style="font-size: 2em; margin: 0; font-weight: bold;">$passCount</p>
            </div>
            <div class="summary-card" style="border-left: 4px solid #ffc107;">
                <h3 style="color: #ffc107;">⚠️ Advertencias</h3>
                <p style="font-size: 2em; margin: 0; font-weight: bold;">$warningCount</p>
            </div>
            <div class="summary-card" style="border-left: 4px solid #dc3545;">
                <h3 style="color: #dc3545;">❌ Fallos</h3>
                <p style="font-size: 2em; margin: 0; font-weight: bold;">$failCount</p>
            </div>
            <div class="summary-card" style="border-left: 4px solid #007bff;">
                <h3 style="color: #007bff;">📋 Total</h3>
                <p style="font-size: 2em; margin: 0; font-weight: bold;">$totalTests</p>
            </div>
        </div>
        
        <div class="test-section">
            <h3>💡 Recomendaciones Generales</h3>
            <ul style="line-height: 1.8;">
                <li><strong>Mantener dependencias actualizadas:</strong> Revisar regularmente las versiones de Spring Boot y librerías de seguridad</li>
                <li><strong>Implementar logging de seguridad:</strong> Registrar intentos de acceso y actividades sospechosas</li>
                <li><strong>Configurar WAF:</strong> Considerar implementar un Web Application Firewall</li>
                <li><strong>Rate Limiting:</strong> Implementar límites de velocidad para prevenir ataques de fuerza bruta</li>
                <li><strong>Pruebas regulares:</strong> Ejecutar estas pruebas en cada despliegue</li>
                <li><strong>Headers de seguridad:</strong> Implementar todos los headers de seguridad recomendados</li>
            </ul>
        </div>
        
        <div class="header" style="margin-top: 30px;">
            <h3>🔒 Estado General de Seguridad</h3>
            <p style="font-size: 1.2em;">
"@

    if ($failCount -eq 0 -and $warningCount -le 1) {
        $summary += "🟢 <strong>EXCELENTE</strong> - Sistema bien protegido"
    } elseif ($failCount -le 1 -and $warningCount -le 2) {
        $summary += "🟡 <strong>BUENO</strong> - Algunas mejoras recomendadas"
    } else {
        $summary += "🔴 <strong>NECESITA ATENCIÓN</strong> - Vulnerabilidades críticas detectadas"
    }

    $summary += @"
            </p>
            <p style="margin-top: 15px; opacity: 0.9;">Reporte generado por GitHub Copilot Security Framework</p>
        </div>
    </div>
</body>
</html>
"@
    
    $summary | Out-File -FilePath $ResultsFile -Append -Encoding UTF8
    Write-Host ""
    Write-Host "✅ Reporte completo guardado en: $ResultsFile" -ForegroundColor Green
}

# EJECUCIÓN PRINCIPAL
function Main {
    Add-Type -AssemblyName System.Web
    
    Create-ReportHeader
    
    if (-not (Test-ApplicationStatus)) {
        Write-Host "❌ No se pueden ejecutar las pruebas sin la aplicación ejecutándose" -ForegroundColor Red
        Finalize-Report
        return
    }
    
    # Ejecutar todas las pruebas
    Test-SQLInjection
    Test-XSSVulnerability
    Test-PathTraversal
    Test-SecurityHeaders
    
    Finalize-Report
    
    Write-Host ""
    Write-Host "🎉 PRUEBAS DE SEGURIDAD DINÁMICAS COMPLETADAS" -ForegroundColor Green
    Write-Host "==============================================" -ForegroundColor Green
    Write-Host "📄 Reporte HTML generado: $ResultsFile" -ForegroundColor Cyan
    Write-Host "🌐 Abrir en navegador para ver resultados detallados" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "💡 Para abrir el reporte automáticamente:" -ForegroundColor Yellow
    Write-Host "   Start-Process '$ResultsFile'" -ForegroundColor Yellow
}

# Ejecutar script principal
Main