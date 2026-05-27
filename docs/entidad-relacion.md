# Entidad-Relacion

## Diagrama Entidad-Relacion (ERD)

```mermaid
erDiagram
  GREENHOUSE ||--o{ CROP : contiene
  GREENHOUSE ||--o{ SENSOR : instala
  GREENHOUSE ||--o{ ZONE : contiene
  GREENHOUSE ||--o{ ACTUATOR : contiene
  GREENHOUSE ||--o{ AUTOMATION_RULE : tiene
  GREENHOUSE ||--o{ IRRIGATION_EVENT : recibe
  SENSOR ||--o{ READING : registra
  SENSOR ||--o{ ALERT : dispara
  ZONE ||--o{ SENSOR : alberga

  GREENHOUSE {
    long id PK
    string name
    string location
    decimal area_square_meters
    boolean active
  }

  CROP {
    long id PK
    string name
    string variety
    date planted_at
    date expected_harvest_at
    string status
    long greenhouse_id FK
  }

  SENSOR {
    long id PK
    string code UK
    string type
    string unit
    decimal min_threshold
    decimal max_threshold
    long greenhouse_id FK
    long zone_id FK
  }

  READING {
    long id PK
    decimal reading_value
    datetime recorded_at
    long sensor_id FK
  }

  IRRIGATION_EVENT {
    long id PK
    datetime started_at
    integer duration_minutes
    decimal water_liters
    string mode
    long greenhouse_id FK
  }

  ALERT {
    long id PK
    string severity
    string message
    boolean resolved
    datetime created_at
    long sensor_id FK
  }

  ACTUATOR {
    long id PK
    string name
    string type
    boolean enabled
    boolean active
    long greenhouse_id FK
  }

  AUTOMATION_RULE {
    long id PK
    string name
    string type
    decimal threshold
    boolean enabled
    long greenhouse_id FK
  }

  ZONE {
    long id PK
    string name
    string description
    boolean active
    long greenhouse_id FK
  }

  AUDIT_LOG {
    long id PK
    string action
    string detail
    datetime created_at
    string origin
  }

  APP_USER {
    long id PK
    string email UK
    string full_name
    string password_hash
    string provider
    string role
    boolean verified
  }
```

## Relaciones

| Entidad Origen | Entidad Destino | Tipo | FK | Cardinalidad | Descripcion |
|----------------|-----------------|------|-----|-------------|-------------|
| Greenhouse | Crop | OneToMany | greenhouse_id | 1:N | Un invernadero contiene muchos cultivos |
| Greenhouse | Sensor | OneToMany | greenhouse_id | 1:N | Un invernadero instala muchos sensores |
| Greenhouse | Zone | OneToMany | greenhouse_id | 1:N | Un invernadero contiene muchas zonas |
| Greenhouse | Actuator | OneToMany | greenhouse_id | 1:N | Un invernadero tiene muchos actuadores |
| Greenhouse | AutomationRule | OneToMany | greenhouse_id | 1:N | Un invernadero tiene muchas reglas |
| Greenhouse | IrrigationEvent | OneToMany | greenhouse_id | 1:N | Un invernadero recibe muchos riegos |
| Sensor | Reading | OneToMany | sensor_id | 1:N | Un sensor registra muchas lecturas |
| Sensor | Alert | OneToMany | sensor_id | 1:N | Un sensor dispara muchas alertas |
| Zone | Sensor | OneToMany | zone_id | 1:N | Una zona alberga muchos sensores |
| Crop | Greenhouse | ManyToOne | greenhouse_id | N:1 | Muchos cultivos pertenecen a un invernadero |
| Sensor | Greenhouse | ManyToOne | greenhouse_id | N:1 | Muchos sensores pertenecen a un invernadero |
| Sensor | Zone | ManyToOne | zone_id | N:1 | Muchos sensores pertenecen a una zona |
| Reading | Sensor | ManyToOne | sensor_id | N:1 | Muchas lecturas pertenecen a un sensor |
| IrrigationEvent | Greenhouse | ManyToOne | greenhouse_id | N:1 | Muchos riegos pertenecen a un invernadero |
| Alert | Sensor | ManyToOne | sensor_id | N:1 | Muchas alertas pertenecen a un sensor |
| Actuator | Greenhouse | ManyToOne | greenhouse_id | N:1 | Muchos actuadores pertenecen a un invernadero |
| AutomationRule | Greenhouse | ManyToOne | greenhouse_id | N:1 | Muchas reglas pertenecen a un invernadero |
| Zone | Greenhouse | ManyToOne | greenhouse_id | N:1 | Muchas zonas pertenecen a un invernadero |
