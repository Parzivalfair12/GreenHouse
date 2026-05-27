# Diagramas de Arquitectura — GreenHouse Manager

## 1. Arquitectura General

```mermaid
graph TB
    subgraph Frontend["Frontend (React + Vite)"]
        REACT[React SPA]
        DARK[Dark Mode]
        I18N[i18n ES/EN]
        AUTH[AUTH: JWT in localStorage]
    end
    
    subgraph Backend["Backend (Spring Boot 3 — Puerto 8080)"]
        REST[REST API]
        JWT[JWT Filter]
        OAUTH[OAuth2 Google]
        FLYWAY[Flyway Migrations]
    end
    
    subgraph Database["Base de Datos (PostgreSQL 16)"]
        PG[(PostgreSQL)]
        V1[(V1: Schema)]
        V2[(V2: Verified)]
    end
    
    subgraph IA["IA Microservice (Flask — Puerto 5000)"]
        FLASK[Flask App]
        MODEL[scikit-learn Model]
        PREDICT[Predictions]
        ANOMALY[Anomaly Detection]
    end
    
    subgraph IoT["IoT Simulator (Python)"]
        SIM[Simulador]
        TEMP[Temperatura]
        HUM[Humedad]
        LIGHT[Luz]
    end
    
    subgraph DevOps["DevOps"]
        DOCKER[Docker Compose]
        GH[GitHub Actions CI/CD]
        SWAGGER[Swagger UI]
    end

    REACT -->|HTTP + JWT| REST
    REACT --> AUTH
    REST --> JWT
    REST --> OAUTH
    REST --> FLYWAY
    FLYWAY --> PG
    SIM -->|POST /api/readings| REST
    REST -->|HTTP /ia/predict| FLASK
    FLASK --> MODEL
    DOCKER -->|Containers| Backend
    DOCKER -->|Containers| Frontend
    DOCKER -->|Container| Database
    GH -->|4 Jobs CI| Backend
    GH -->|4 Jobs CI| Frontend
    SWAGGER --> REST
```

## 2. Flujo de Autenticacion (OAuth2 + JWT)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant FE as Frontend React
    participant BE as Backend Spring
    participant G as Google OAuth
    
    alt Login con Email
        U->>FE: Ingresa email + password
        FE->>BE: POST /api/auth/login
        BE->>BE: Verifica BCrypt hash
        BE-->>FE: LoginResponse { JWT, email, role }
        FE->>FE: Guarda en localStorage
    else Login con Google
        U->>FE: Clic "Continuar con Google"
        FE->>BE: /oauth2/authorization/google
        BE->>G: Redirect a Google
        G-->>U: Login en Google
        G-->>BE: Authorization code
        BE->>BE: OAuth2LoginSuccessHandler
        BE->>BE: Genera JWT propio
        BE-->>FE: Redirect con JWT en URL
        FE->>FE: Guarda token en localStorage
    end
    
    Note over FE,BE: Requests subsecuentes
    FE->>BE: GET /api/greenhouses + Authorization: Bearer JWT
    BE->>BE: JwtAuthenticationFilter valida token
    BE->>BE: SecurityConfig verifica rol
    BE-->>FE: 200 + datos
```

## 3. Flujo IA (Flask + scikit-learn)

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant BE as Backend Spring
    participant FL as Flask IA
    
    FE->>BE: POST /api/ia/predict {temperatures, humidities}
    BE->>FL: POST /ia/predict (RestTemplate)
    FL->>FL: Carga modelo (regresion lineal)
    FL->>FL: Predice proximo valor
    FL->>FL: Calcula nivel de riesgo
    FL-->>BE: {predictedTemperature, riskLevel}
    BE-->>FE: IaPredictionResponse
    
    FE->>BE: POST /api/ia/recommend {riskLevel}
    BE->>FL: POST /ia/recommend
    FL-->>BE: {action, reason}
    BE-->>FE: IaRecommendationResponse
```

## 4. Flujo IoT (Simulador de Sensores)

```mermaid
sequenceDiagram
    participant IOT as IoT Simulator
    participant BE as Backend Spring
    participant DB as PostgreSQL
    
    loop Cada N segundos
        IOT->>IOT: Genera lectura (temp, hum, luz)
        IOT->>BE: POST /api/readings {sensorId, value}
        BE->>BE: OperationsService.createReading()
        BE->>BE: Evalua umbrales
        alt Fuera de rango
            BE->>BE: Crea alerta
            BE->>BE: AuditLog: "Alerta generada"
        end
        BE->>DB: INSERT reading
        BE-->>IOT: 201 Created
    end
```

## 5. Pipeline CI/CD (GitHub Actions)

```mermaid
graph LR
    PUSH[Push / PR] --> TRIGGER[Trigger CI]
    TRIGGER --> J1[Job 1: Backend JUnit]
    TRIGGER --> J2[Job 2: Frontend Vitest]
    TRIGGER --> J3[Job 3: Python JSON]
    TRIGGER --> J4[Job 4: Python IA]
    
    J1 --> J1S[Setup Java 21]
    J1S --> J1C[Maven Cache]
    J1C --> J1T[mvn test]
    
    J2 --> J2S[Setup Node 20]
    J2S --> J2N[npm cache]
    J2N --> J2L[ESLint]
    J2L --> J2T[Vitest]
    J2T --> J2B[Vite Build]
    
    J3 --> J3S[Setup Python 3.11]
    J3S --> J3T[unittest JSON]
    
    J4 --> J4S[Setup Python 3.11]
    J4S --> J4I[pip install]
    J4I --> J4T[pytest]
    
    J1T --> GREEN[✅ All Green]
    J2B --> GREEN
    J3T --> GREEN
    J4T --> GREEN
```

## 6. Arquitectura Docker

```mermaid
graph TB
    subgraph Docker[ Docker Compose]
        subgraph Net[greenhouse-network]
            PG[postgres:16-alpine<br/>:5432]
            BE[backend<br/>greenhouse-api:8080]
            FE[frontend<br/>nginx:80 → :5173]
        end
    end
    
    USER[Usuario] -->|http://localhost:5173| FE
    FE -->|/api/ → proxy_pass| BE
    BE -->|JDBC| PG
    BE -->|HTTP| FLASK[Flask IA<br/>:5000]
    
    PG --> VOL[(PostgreSQL Volume)]
```

## 7. Arquitectura Backend (Spring Boot)

```mermaid
graph TB
    subgraph Layers[Capas del Backend]
        WEB[Controller Layer<br/>6 Controllers]
        DTO[DTO Layer<br/>22 DTOs]
        SVC[Service Layer<br/>5 Services]
        REPO[Repository Layer<br/>9 Repositories]
        DOM[Domain Layer<br/>19 Entities + Enums]
    end
    
    subgraph Security[Seguridad]
        SC[SecurityConfig]
        JWT[JwtAuthenticationFilter]
        JWP[JwtTokenProvider]
        OAH[OAuth2SuccessHandler]
        PRE[ @PreAuthorize]
    end
    
    subgraph Config[Configuracion]
        EX[GlobalExceptionHandler]
        FL[Flyway]
        CORS[CORS Config]
        I18[Internationalization]
        OA[OpenAPI/Swagger]
    end
    
    WEB --> DTO
    DTO --> SVC
    SVC --> REPO
    REPO --> DOM
    WEB --> PRE
    PRE --> SC
    SC --> JWT
    SC --> OAH
    JWT --> JWP
```

## 8. Flujo de Automatizacion Taiga

```mermaid
graph TB
    subgraph Taiga[Modulo Taiga]
        TC[TaigaController]
        TS[TaigaService]
        ST[6 User Stories]
        CR[18 Criterios]
    end
    
    subgraph Front[Frontend React]
        TSec[TaigaSection]
        TCard[Story Cards]
        TMet[Metrics]
    end
    
    subgraph CI[GitHub Actions]
        BUILD[Build]
        TEST[Test]
        LINT[Lint]
        IA[IA Test]
    end
    
    TC --> ST
    TC --> CR
    ST --> TSec
    CR --> TCard
    TS --> TMet
    BUILD -->|verde| EVIDENCE[Evidencia]
    TEST -->|verde| EVIDENCE
    LINT -->|verde| EVIDENCE
    IA -->|verde| EVIDENCE
```
