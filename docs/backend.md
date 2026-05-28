# Backend — GreenHouse Manager

## Stack Tecnológico

- **Lenguaje:** Java 21+
- **Framework:** Spring Boot 3.3.5
- **Base de Datos:** PostgreSQL 16 (producción) / H2 (tests)
- **ORM:** Hibernate 6 + JPA
- **Migraciones:** Flyway
- **Seguridad:** Spring Security + JWT (jjwt) + OAuth2 Google
- **API Docs:** SpringDoc OpenAPI (Swagger UI)
- **Testing:** JUnit 5 + MockMvc + AssertJ
- **Build:** Maven

## Estructura de Paquetes

```
backend/src/main/java/com/example/greenhouse/
├── GreenhouseApplication.java     # Punto de entrada
├── config/                        # Configuraciones del sistema
│   ├── SecurityConfig.java        # Seguridad HTTP, CORS, filtros
│   ├── JwtTokenProvider.java       # Generación y validación JWT
│   ├── JwtAuthenticationFilter.java# Filtro de autenticación JWT
│   ├── OAuth2ClientConfig.java    # Configuración OAuth2
│   ├── OAuth2LoginSuccessHandler.java # Manejador éxito OAuth
│   ├── OpenApiConfig.java         # Configuración Swagger/OpenAPI
│   ├── InternationalizationConfig.java  # i18n Accept-Language
│   ├── RateLimitingFilter.java    # Límite de intentos por IP
│   ├── SecurityHeadersFilter.java # Headers de seguridad HTTP
│   ├── DataSeeder.java            # Datos demo iniciales
│   ├── GlobalExceptionHandler.java# Manejador global de excepciones
│   └── ModelValidator.java        # Validador del modelo JPA
├── domain/                        # Entidades JPA y enums
│   ├── AppUser.java               # Usuario del sistema
│   ├── Greenhouse.java            # Invernadero
│   ├── Sensor.java                # Sensor
│   ├── Reading.java               # Lectura de sensor
│   ├── Alert.java                 # Alerta operativa
│   ├── Actuator.java              # Actuador (riego, ventilador)
│   ├── AutomationRule.java        # Regla de automatización
│   ├── Crop.java                  # Cultivo
│   ├── IrrigationEvent.java       # Evento de riego
│   ├── Zone.java                  # Zona del invernadero
│   ├── AuditLog.java              # Bitácora de auditoría
│   └── ... enums
├── repository/                    # Interfaces Spring Data JPA
├── service/                       # Lógica de negocio
│   ├── AuthService.java           # Autenticación
│   ├── RuleEngineService.java     # Motor de reglas
│   ├── SimulationService.java     # Simulación IoT
│   ├── IaService.java             # Proxy IA Flask
│   ├── EmailService.java          # Correos transaccionales
│   ├── AuditLogService.java       # Bitácora
│   ├── GreenhouseService.java     # CRUD invernaderos
│   ├── OperationsService.java     # Operaciones CRUD genéricas
│   ├── AlertService.java          # Alertas
│   └── AiPredictionService.java   # Predicción IA local
└── web/                           # Controladores REST
    ├── dto/                       # Data Transfer Objects
    ├── AuthController.java        # Endpoints de autenticación
    ├── GreenhouseController.java  # CRUD invernaderos
    ├── UserController.java        # Administración usuarios
    ├── AlertController.java       # Alertas
    ├── SimulatorController.java   # Simulador IoT
    ├── IaController.java          # IA predictiva
    └── DashboardController.java   # Dashboard
```

## Servicios Principales

### AuthService
Maneja registro, login JWT, OAuth2 Google, verificación de email y recuperación de contraseña. Todos los mensajes de error se resuelven con i18n.

### RuleEngineService
Evalúa lecturas de sensores contra umbrales y reglas de automatización. Crea alertas y activa actuadores.

### SimulationService
Genera lecturas simuladas cada 5 segundos con valores graduales y anomalías ocasionales para probar el sistema.

### OperationsService
CRUD de zonas, sensores, lecturas, actuadores y reglas con auditoría automática.

### AiPredictionService
Predicción local usando media móvil ponderada y proyección lineal sobre lecturas históricas.

## Configuración

### application.properties

```properties
app.jwt-secret=clave-secreta-de-al-menos-32-caracteres
app.jwt-expiration-ms=86400000
app.frontend-url=http://localhost:5173
spring.datasource.url=jdbc:postgresql://localhost:5432/greenhouse
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
greenhouse.email.enabled=false
```

## Pruebas

```bash
mvn test                    # Todos los tests (48)
mvn test -Dtest=AuthControllerTest  # Test específico
mvn clean package           # Build completo
```
