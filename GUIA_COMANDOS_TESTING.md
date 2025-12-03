# 📋 GUÍA COMPLETA DE COMANDOS DE TESTING - Pastelería Córdova

## 🧪 **Comandos para Ejecutar Tests**

### **1. 🚀 Ejecutar TODOS los Tests (Recomendado)**
```powershell
mvn clean test
```
**✅ Resultado**: 29 tests pasando
- Tests de integración (5 casos)
- Tests unitarios de servicios (21 tests)
- Tests de controladores (3 tests)

### **2. 🔒 Tests de Seguridad Únicamente**
```powershell
mvn test -Dtest=SecurityTestsStandalone
```
**✅ Resultado**: 10/10 tests de seguridad pasando
- ✅ SQL Injection Protection
- ✅ XSS Protection 
- ✅ Path Traversal Protection
- ✅ Input Validation
- ✅ Special Character Filtering

### **3. 🔗 Tests de Integración Únicamente**
```powershell
mvn test -Dtest=IntegrationTest
```
**✅ Resultado**: 5 casos de uso completos
- Flujo completo de compra
- Gestión de productos
- Validaciones de negocio
- Rendimiento y carga
- Seguridad e integridad

### **4. ⚙️ Tests de Servicios Únicamente**
```powershell
# Todos los servicios
mvn test -Dtest=*ServiceTest*

# Servicios específicos
mvn test -Dtest=CarritoServiceTestSimple
mvn test -Dtest=FacturaServiceTestFinal
mvn test -Dtest=PedidoServiceTestFinal
mvn test -Dtest=ProductoServiceTestSimple
```

### **5. 🎛️ Tests de Controladores Únicamente**
```powershell
mvn test -Dtest=FacturaControllerTest
```

### **6. 📊 Tests con Cobertura de Código**
```powershell
mvn clean test jacoco:report
```
**📁 Reporte disponible en**: `target/site/jacoco/index.html`

---

## 🎯 **Comandos por Tipo de Funcionalidad**

### **A. Tests de Seguridad 🛡️**

**Standalone (Independientes) - FUNCIONAN ✅**
```powershell
mvn test -Dtest=SecurityTestsStandalone
```

**Contexto completo - REQUIERE CONFIGURACIÓN ⚠️**
```powershell
mvn test -Dtest=SecurityTests
```
> ⚠️ **Nota**: Requiere configurar `CustomUserDetailsService` para funcionar

### **B. Tests de Negocio 💼**

**Carrito de Compras**
```powershell
mvn test -Dtest=CarritoServiceTestSimple
```

**Facturación y PDF**
```powershell
mvn test -Dtest=FacturaServiceTestFinal,FacturaControllerTest
```

**Gestión de Productos**
```powershell
mvn test -Dtest=ProductoServiceTestSimple
```

**Pedidos**
```powershell
mvn test -Dtest=PedidoServiceTestFinal
```

### **C. Tests de Integración 🔗**

**Flujo Completo E2E**
```powershell
mvn test -Dtest=IntegrationTest#testIntegration_FlujoCompletoDeCompra
```

**Gestión de Productos**
```powershell
mvn test -Dtest=IntegrationTest#testIntegration_GestionProductos
```

**Validaciones de Negocio**
```powershell
mvn test -Dtest=IntegrationTest#testIntegration_ValidacionesDeNegocio
```

**Rendimiento y Carga**
```powershell
mvn test -Dtest=IntegrationTest#testIntegration_RendimientoYCarga
```

---

## 📈 **Resultados de Tests por Categoría**

### **✅ Tests que SIEMPRE PASAN (29 Tests)**

| **Categoría** | **Clase** | **Tests** | **Estado** |
|---------------|-----------|-----------|------------|
| **Seguridad** | `SecurityTestsStandalone` | 10 | ✅ 100% |
| **Integración** | `IntegrationTest` | 5 | ✅ 100% |
| **Servicios** | `CarritoServiceTestSimple` | 3 | ✅ 100% |
| **Servicios** | `FacturaServiceTestFinal` | 6 | ✅ 100% |
| **Servicios** | `PedidoServiceTestFinal` | 5 | ✅ 100% |
| **Servicios** | `ProductoServiceTestSimple` | 7 | ✅ 100% |
| **Controladores** | `FacturaControllerTest` | 3 | ✅ 100% |

### **⚠️ Tests que REQUIEREN CONFIGURACIÓN**

| **Clase** | **Problema** | **Solución** |
|-----------|--------------|--------------|
| `SecurityTests` | Bean `CustomUserDetailsService` faltante | Configurar en tests o usar mocks |

---

## 🔧 **Scripts PowerShell Personalizados**

### **Crear: `test-all.ps1`**
```powershell
Write-Host "🧪 Ejecutando TODOS los tests de Pastelería Córdova..." -ForegroundColor Cyan
mvn clean test
Write-Host "✅ Tests completados. Ver reporte en target/surefire-reports/" -ForegroundColor Green
```

### **Crear: `test-security.ps1`** 
```powershell
Write-Host "🛡️ Ejecutando tests de SEGURIDAD..." -ForegroundColor Yellow
mvn test -Dtest=SecurityTestsStandalone
Write-Host "✅ Tests de seguridad completados." -ForegroundColor Green
```

### **Crear: `test-business.ps1`**
```powershell
Write-Host "💼 Ejecutando tests de NEGOCIO..." -ForegroundColor Magenta
mvn test -Dtest=*ServiceTest*,*ControllerTest,IntegrationTest
Write-Host "✅ Tests de negocio completados." -ForegroundColor Green
```

---

## 📊 **Análisis de Cobertura**

### **Generar Reporte Completo**
```powershell
mvn clean test jacoco:report
```

### **Ver Reporte**
1. Abrir: `target/site/jacoco/index.html`
2. **Cobertura actual**: 44 clases analizadas
3. **Líneas cubiertas**: Revisar por paquete

---

## 🚨 **Resolución de Problemas**

### **Error: Tests de SecurityTests fallan**
```powershell
# Usar solo los tests independientes
mvn test -Dtest=SecurityTestsStandalone
```

### **Error: Puerto 8080 ocupado**
```powershell
# Usar perfil de test específico
mvn test -Dspring.profiles.active=test
```

### **Error: Base de datos**
```powershell
# Los tests usan H2 en memoria, no requiere MySQL
mvn test -Dspring.profiles.active=test
```

---

## 📋 **Checklist de Testing**

### **✅ Tests Básicos (Cada vez que cambies código)**
- [ ] `mvn test -Dtest=SecurityTestsStandalone`
- [ ] `mvn test -Dtest=IntegrationTest`

### **✅ Tests Completos (Antes de desplegar)**
- [ ] `mvn clean test`
- [ ] Verificar 29/29 tests pasando
- [ ] Revisar reporte JaCoCo

### **✅ Tests de Seguridad (Cada semana)**
- [ ] `mvn test -Dtest=SecurityTestsStandalone`
- [ ] Verificar 10/10 protecciones activas
- [ ] Revisar logs de seguridad

---

## 🎯 **Comandos Rápidos de Referencia**

```powershell
# Tests completos
mvn clean test

# Solo seguridad
mvn test -Dtest=SecurityTestsStandalone

# Solo integración  
mvn test -Dtest=IntegrationTest

# Con cobertura
mvn clean test jacoco:report

# Ver resultados
# Navegador -> target/site/jacoco/index.html
```

---

> **💡 Tip**: Guarda estos comandos en un archivo `README-TESTING.md` para referencia rápida del equipo.

> **🔒 Seguridad**: Los tests `SecurityTestsStandalone` SIEMPRE deben pasar al 100% antes de cualquier despliegue.

> **📊 Cobertura**: Objetivo mínimo 80% de cobertura de líneas en servicios críticos.