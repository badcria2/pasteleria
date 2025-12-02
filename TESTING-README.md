# 🧪 Pruebas Unitarias - Pastelería Córdova

## 🚀 GUÍA DE EJECUCIÓN RÁPIDA

### ⚡ **Comandos Esenciales:**
```bash
# Ejecutar TODOS los tests
mvn clean test

# Solo tests de seguridad (SQL Injection incluido)
mvn test -Dtest="*Security*"

# Análisis estático + Tests
mvn clean compile test spotbugs:spotbugs

# Cobertura de código
mvn jacoco:prepare-agent test jacoco:report
```

### 🔒 **Validación Crítica SQL Injection:**
```bash
# Terminal 1: Iniciar app
mvn spring-boot:run

# Terminal 2: Validar corrección
.\test_security_fix.ps1

# Resultado esperado: Todos los payloads BLOQUEADOS ✅
```

### 📊 **Payloads SQL Injection Probados:**
- `admin' OR '1'='1` → ✅ **BLOQUEADO**
- `admin' UNION SELECT 1,2,3--` → ✅ **BLOQUEADO**
- `admin'/**/OR/**/1=1--` → ✅ **BLOQUEADO** 
- `' OR 'x'='x` → ✅ **BLOQUEADO**
- `admin'; DROP TABLE usuarios;--` → ✅ **BLOQUEADO**

---

## Testing con JUnit 5 y Mockito

### 🚀 Pruebas Implementadas

#### **Servicios Críticos Testados:**

1. **PedidoServiceTest** - 9 pruebas
   - ✅ Crear pedido desde carrito (exitoso/carrito vacío)
   - ✅ Actualizar estado de pedido (exitoso/pedido no encontrado)
   - ✅ Obtener pedidos por cliente
   - ✅ Buscar pedidos por ID y estado
   - ✅ Contar pedidos por estado

2. **ProductoServiceTest** - 12 pruebas  
   - ✅ CRUD completo de productos
   - ✅ Búsqueda de productos
   - ✅ Validación y reducción de stock
   - ✅ Obtener productos recientes

3. **FacturaServiceTest** - 11 pruebas
   - ✅ Generación de PDF (exitoso/errores)
   - ✅ Validación de estados para facturación
   - ✅ Generación de nombres de archivo
   - ✅ Obtención de estados de factura

4. **CarritoServiceTest** - 13 pruebas
   - ✅ Agregar/actualizar productos en carrito
   - ✅ Validación de stock
   - ✅ Cálculo de totales y conteos
   - ✅ Operaciones de carrito (limpiar, eliminar)

5. **FacturaControllerTest** - 8 pruebas
   - ✅ Endpoints de descarga y previsualización
   - ✅ Validación de permisos y autenticación
   - ✅ Seguridad (admin vs cliente)

### 🛠️ Comandos para Ejecutar Pruebas

#### Ejecutar todas las pruebas:
```bash
mvn test
```

#### Ejecutar pruebas de un servicio específico:
```bash
mvn test -Dtest=PedidoServiceTest
mvn test -Dtest=ProductoServiceTest
mvn test -Dtest=FacturaServiceTest
```

#### Ejecutar con reporte de cobertura:
```bash
mvn test jacoco:report
```

#### Ejecutar solo pruebas unitarias (excluyendo integración):
```bash
mvn test -Dtest="**/*Test"
```

### 📊 Tecnologías de Testing

- **JUnit 5** - Framework principal de testing
- **Mockito** - Mocking y stubbing  
- **Spring Boot Test** - Testing de contexto Spring
- **MockMvc** - Testing de controllers web
- **H2 Database** - Base de datos en memoria para tests
- **Spring Security Test** - Testing de seguridad

### 🎯 Cobertura de Pruebas

Las pruebas cubren:
- ✅ **Casos exitosos** - Flujos normales de la aplicación
- ✅ **Casos de error** - Validaciones y excepciones
- ✅ **Casos límite** - Stock insuficiente, datos no encontrados
- ✅ **Seguridad** - Permisos y autenticación
- ✅ **Validaciones** - Estados, formatos, rangos

### 🔧 Configuración de Test

#### application-test.properties
- Base de datos H2 en memoria
- Logging optimizado para tests
- Configuración de seguridad de prueba
- Thymeleaf sin cache

#### Estructura de Directorios
```
src/test/java/com/pasteleria/cordova/
├── service/           # Pruebas de lógica de negocio
│   ├── PedidoServiceTest.java
│   ├── ProductoServiceTest.java
│   ├── FacturaServiceTest.java
│   └── CarritoServiceTest.java
├── controller/        # Pruebas de endpoints web
│   └── FacturaControllerTest.java
└── PasteleriaCordovaApplicationTests.java
```

### 📈 Métricas de Testing

- **53 pruebas unitarias** implementadas
- **5 clases críticas** cubiertas
- **Mocking completo** de dependencias
- **Validación de seguridad** incluida
- **Testing de excepciones** cubierto

### 🚦 Ejecución Continua

Para desarrollo activo, usar:
```bash
# Ejecutar tests en modo continuo
mvn test -Dtest="**/*Test" -DfailIfNoTests=false --watch
```

### 🎉 Beneficios

1. **Detección temprana** de errores
2. **Refactoring seguro** con confianza
3. **Documentación viva** del comportamiento
4. **Integración con CI/CD** lista
5. **Cobertura de casos críticos** completa