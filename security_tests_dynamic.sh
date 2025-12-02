#!/bin/bash
# Script de Pruebas de Seguridad Dinámicas - Pastelería Córdova
# Ejecutar con: bash security_tests_dynamic.sh

echo "🛡️  INICIANDO PRUEBAS DE SEGURIDAD DINÁMICAS"
echo "=============================================="
echo ""

# Configuración
BASE_URL="http://localhost:8080"
RESULTS_FILE="security_test_results_$(date +%Y%m%d_%H%M%S).html"

# Función para crear header del reporte
create_report_header() {
    cat > "$RESULTS_FILE" << 'EOF'
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pruebas de Seguridad Dinámicas - Pastelería Córdova</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 20px; }
        .test-section { margin: 20px 0; padding: 15px; border-left: 4px solid #007bff; background: #f8f9fa; }
        .test-pass { border-left-color: #28a745; background: #f8fff9; }
        .test-fail { border-left-color: #dc3545; background: #fff5f5; }
        .test-warning { border-left-color: #ffc107; background: #fff9f0; }
        .code-block { background: #2d3748; color: #e2e8f0; padding: 15px; border-radius: 5px; overflow-x: auto; font-family: 'Courier New', monospace; }
        .metric { display: inline-block; margin: 10px; padding: 15px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); text-align: center; }
        .status-pass { color: #28a745; font-weight: bold; }
        .status-fail { color: #dc3545; font-weight: bold; }
        .status-warning { color: #ffc107; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔍 Pruebas de Seguridad Dinámicas</h1>
            <p>Pastelería Córdova - Análisis en Vivo</p>
            <p>Fecha: $(date)</p>
        </div>
EOF
    echo "📄 Reporte iniciado: $RESULTS_FILE"
}

# Función para agregar resultado al reporte
add_test_result() {
    local test_name="$1"
    local status="$2"
    local description="$3"
    local details="$4"
    
    local class_name="test-section"
    case "$status" in
        "PASS") class_name="test-section test-pass" ;;
        "FAIL") class_name="test-section test-fail" ;;
        "WARNING") class_name="test-section test-warning" ;;
    esac
    
    cat >> "$RESULTS_FILE" << EOF
        <div class="$class_name">
            <h3>$test_name - <span class="status-$(echo $status | tr '[:upper:]' '[:lower:]')">$status</span></h3>
            <p>$description</p>
            <div class="code-block">$details</div>
        </div>
EOF
}

# Función para verificar si la aplicación está ejecutándose
check_app_status() {
    echo "🔍 Verificando estado de la aplicación..."
    
    local response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL" 2>/dev/null || echo "000")
    
    if [ "$response" = "000" ]; then
        echo "❌ ERROR: La aplicación no está ejecutándose en $BASE_URL"
        add_test_result "Estado de la Aplicación" "FAIL" "La aplicación no responde en el puerto 8080" "HTTP Code: $response"
        return 1
    else
        echo "✅ Aplicación ejecutándose - HTTP $response"
        add_test_result "Estado de la Aplicación" "PASS" "Aplicación respondiendo correctamente" "HTTP Code: $response"
        return 0
    fi
}

# Prueba 1: SQL Injection en Login
test_sql_injection() {
    echo ""
    echo "🔍 Prueba 1: SQL Injection en Login"
    echo "-----------------------------------"
    
    local payloads=(
        "admin' OR '1'='1"
        "admin' OR '1'='1' --"
        "admin' OR '1'='1' /*"
        "'; DROP TABLE usuarios; --"
        "admin' UNION SELECT * FROM usuarios --"
    )
    
    local results=""
    local vulnerable=false
    
    for payload in "${payloads[@]}"; do
        echo "Probando payload: $payload"
        
        local response=$(curl -s -X POST "$BASE_URL/login" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "username=$payload&password=test" \
            -w "%{http_code}" \
            -o /dev/null 2>/dev/null || echo "000")
        
        results="$results\nPayload: $payload -> HTTP $response"
        
        if [[ "$response" = "200" || "$response" = "302" ]]; then
            vulnerable=true
        fi
    done
    
    if [ "$vulnerable" = true ]; then
        add_test_result "SQL Injection - Login" "FAIL" "Posible vulnerabilidad SQL Injection detectada" "$results"
        echo "❌ VULNERABILIDAD DETECTADA"
    else
        add_test_result "SQL Injection - Login" "PASS" "No se detectó vulnerabilidad SQL Injection" "$results"
        echo "✅ SEGURO"
    fi
}

# Prueba 2: XSS en búsqueda
test_xss_vulnerability() {
    echo ""
    echo "🔍 Prueba 2: Cross-Site Scripting (XSS)"
    echo "---------------------------------------"
    
    local payloads=(
        "<script>alert('XSS')</script>"
        "<img src=x onerror=alert('XSS')>"
        "javascript:alert('XSS')"
        "<svg onload=alert('XSS')>"
        "'><script>alert('XSS')</script>"
    )
    
    local results=""
    local vulnerable=false
    
    for payload in "${payloads[@]}"; do
        echo "Probando XSS payload: $payload"
        
        local response=$(curl -s "$BASE_URL/productos/buscar?q=$(echo "$payload" | sed 's/ /%20/g')" 2>/dev/null)
        
        if echo "$response" | grep -q "<script>" 2>/dev/null; then
            vulnerable=true
            results="$results\nVULNERABLE: $payload encontrado sin sanitizar"
        else
            results="$results\nSEGURO: $payload sanitizado correctamente"
        fi
    done
    
    if [ "$vulnerable" = true ]; then
        add_test_result "Cross-Site Scripting (XSS)" "FAIL" "Vulnerabilidad XSS detectada" "$results"
        echo "❌ VULNERABILIDAD XSS DETECTADA"
    else
        add_test_result "Cross-Site Scripting (XSS)" "PASS" "Protección XSS funcionando correctamente" "$results"
        echo "✅ PROTECCIÓN XSS ACTIVA"
    fi
}

# Prueba 3: Path Traversal
test_path_traversal() {
    echo ""
    echo "🔍 Prueba 3: Path Traversal"
    echo "---------------------------"
    
    local payloads=(
        "../../../etc/passwd"
        "..\\..\\..\\windows\\system32\\drivers\\etc\\hosts"
        "....//....//....//etc/passwd"
        "..%2F..%2F..%2Fetc%2Fpasswd"
        "..%252F..%252F..%252Fetc%252Fpasswd"
    )
    
    local results=""
    local vulnerable=false
    
    for payload in "${payloads[@]}"; do
        echo "Probando Path Traversal: $payload"
        
        local response=$(curl -s "$BASE_URL/uploads/$payload" -w "%{http_code}" 2>/dev/null)
        local http_code=$(echo "$response" | tail -c 4)
        
        results="$results\nPayload: $payload -> HTTP $http_code"
        
        if echo "$response" | grep -q "root:" 2>/dev/null; then
            vulnerable=true
        fi
    done
    
    if [ "$vulnerable" = true ]; then
        add_test_result "Path Traversal" "FAIL" "Vulnerabilidad Path Traversal detectada" "$results"
        echo "❌ VULNERABILIDAD PATH TRAVERSAL"
    else
        add_test_result "Path Traversal" "PASS" "Protección contra Path Traversal activa" "$results"
        echo "✅ PROTEGIDO CONTRA PATH TRAVERSAL"
    fi
}

# Prueba 4: CSRF
test_csrf_protection() {
    echo ""
    echo "🔍 Prueba 4: Protección CSRF"
    echo "----------------------------"
    
    # Intentar realizar acciones sin token CSRF
    local response=$(curl -s -X POST "$BASE_URL/admin/productos/crear" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "nombre=ProductoTest&precio=100" \
        -w "%{http_code}" \
        -o /dev/null 2>/dev/null || echo "000")
    
    local results="POST sin CSRF token -> HTTP $response"
    
    if [[ "$response" = "403" ]]; then
        add_test_result "Protección CSRF" "PASS" "Token CSRF requerido correctamente" "$results"
        echo "✅ PROTECCIÓN CSRF ACTIVA"
    else
        add_test_result "Protección CSRF" "WARNING" "Verificar configuración CSRF" "$results"
        echo "⚠️ VERIFICAR CONFIGURACIÓN CSRF"
    fi
}

# Prueba 5: Headers de Seguridad
test_security_headers() {
    echo ""
    echo "🔍 Prueba 5: Headers de Seguridad"
    echo "---------------------------------"
    
    local headers_response=$(curl -s -I "$BASE_URL" 2>/dev/null)
    local results=""
    local score=0
    local total=6
    
    # Verificar headers importantes
    if echo "$headers_response" | grep -qi "X-Frame-Options"; then
        results="$results\n✅ X-Frame-Options: Presente"
        ((score++))
    else
        results="$results\n❌ X-Frame-Options: Ausente"
    fi
    
    if echo "$headers_response" | grep -qi "X-Content-Type-Options"; then
        results="$results\n✅ X-Content-Type-Options: Presente"
        ((score++))
    else
        results="$results\n❌ X-Content-Type-Options: Ausente"
    fi
    
    if echo "$headers_response" | grep -qi "X-XSS-Protection"; then
        results="$results\n✅ X-XSS-Protection: Presente"
        ((score++))
    else
        results="$results\n❌ X-XSS-Protection: Ausente"
    fi
    
    if echo "$headers_response" | grep -qi "Strict-Transport-Security"; then
        results="$results\n✅ HSTS: Presente"
        ((score++))
    else
        results="$results\n❌ HSTS: Ausente"
    fi
    
    if echo "$headers_response" | grep -qi "Content-Security-Policy"; then
        results="$results\n✅ CSP: Presente"
        ((score++))
    else
        results="$results\n❌ CSP: Ausente"
    fi
    
    if echo "$headers_response" | grep -qi "Referrer-Policy"; then
        results="$results\n✅ Referrer-Policy: Presente"
        ((score++))
    else
        results="$results\n❌ Referrer-Policy: Ausente"
    fi
    
    results="$results\n\nPuntuación: $score/$total headers implementados"
    
    if [ $score -ge 4 ]; then
        add_test_result "Headers de Seguridad" "PASS" "Buena configuración de headers de seguridad" "$results"
        echo "✅ HEADERS DE SEGURIDAD: $score/$total"
    elif [ $score -ge 2 ]; then
        add_test_result "Headers de Seguridad" "WARNING" "Configuración parcial de headers de seguridad" "$results"
        echo "⚠️ HEADERS PARCIALES: $score/$total"
    else
        add_test_result "Headers de Seguridad" "FAIL" "Headers de seguridad insuficientes" "$results"
        echo "❌ HEADERS INSUFICIENTES: $score/$total"
    fi
}

# Función para finalizar el reporte
finalize_report() {
    cat >> "$RESULTS_FILE" << 'EOF'
        <div class="header" style="margin-top: 40px;">
            <h2>📊 Resumen de Resultados</h2>
            <p>Pruebas completadas exitosamente</p>
        </div>
        
        <div class="test-section">
            <h3>💡 Recomendaciones</h3>
            <ul>
                <li>Mantener todas las dependencias actualizadas</li>
                <li>Implementar logging de seguridad para monitoreo</li>
                <li>Realizar pruebas de penetración regulares</li>
                <li>Configurar WAF (Web Application Firewall) si es posible</li>
                <li>Implementar rate limiting para prevenir ataques de fuerza bruta</li>
            </ul>
        </div>
    </div>
</body>
</html>
EOF

    echo ""
    echo "✅ Reporte completo guardado en: $RESULTS_FILE"
}

# EJECUCIÓN PRINCIPAL
main() {
    create_report_header
    
    if ! check_app_status; then
        echo "❌ No se pueden ejecutar las pruebas sin la aplicación ejecutándose"
        finalize_report
        exit 1
    fi
    
    # Ejecutar todas las pruebas
    test_sql_injection
    test_xss_vulnerability
    test_path_traversal
    test_csrf_protection
    test_security_headers
    
    finalize_report
    
    echo ""
    echo "🎉 PRUEBAS DE SEGURIDAD DINÁMICAS COMPLETADAS"
    echo "=============================================="
    echo "📄 Reporte HTML generado: $RESULTS_FILE"
    echo "🌐 Abrir en navegador para ver resultados detallados"
}

# Ejecutar script principal
main "$@"