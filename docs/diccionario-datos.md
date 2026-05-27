# Diccionario de Datos

## 1. Greenhouse (invernadero)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del invernadero |
| name | String | Requerido, @NotBlank | Nombre visible |
| location | String | Requerido, @NotBlank | Ubicacion fisica |
| areaSquareMeters | BigDecimal | Requerido, @Positive, > 0 | Area en metros cuadrados |
| active | Boolean | Requerido, DEFAULT TRUE | Estado operativo |

**Relaciones:** OneToMany -> Crop, Sensor, Zone, Actuator, AutomationRule, IrrigationEvent

---

## 2. Crop (cultivo)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del cultivo |
| name | String | Requerido, @NotBlank | Nombre comun |
| variety | String | Opcional | Variedad o referencia agricola |
| plantedAt | LocalDate | Opcional | Fecha de siembra |
| expectedHarvestAt | LocalDate | Opcional | Fecha estimada de cosecha |
| status | Enum | Requerido, DEFAULT GERMINATING | Estado del cultivo |
| greenhouse_id | Long | FK -> Greenhouse(id), NOT NULL | Invernadero asociado |

**Relaciones:** ManyToOne -> Greenhouse

---

## 3. Sensor

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del sensor |
| code | String | Unico, requerido, @NotBlank | Codigo fisico del sensor |
| type | Enum | Requerido | Tipo de medicion |
| unit | String | Requerido, @NotBlank | Unidad de medida |
| minThreshold | BigDecimal | Opcional | Limite inferior aceptable |
| maxThreshold | BigDecimal | Opcional | Limite superior aceptable |
| greenhouse_id | Long | FK -> Greenhouse(id), NOT NULL | Invernadero donde esta instalado |
| zone_id | Long | FK -> Zone(id), opcional | Zona donde esta ubicado |

**Relaciones:** ManyToOne -> Greenhouse, Zone; OneToMany -> Reading, Alert

---

## 4. Reading (lectura)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador de lectura |
| reading_value | BigDecimal | Requerido, @NotNull | Valor medido |
| recordedAt | LocalDateTime | Requerido, @NotNull | Fecha y hora de lectura |
| sensor_id | Long | FK -> Sensor(id), NOT NULL | Sensor que genero la lectura |

**Relaciones:** ManyToOne -> Sensor

---

## 5. IrrigationEvent (evento de riego)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del riego |
| startedAt | LocalDateTime | Opcional | Inicio del riego |
| durationMinutes | Integer | Requerido, @Positive, > 0 | Duracion en minutos |
| waterLiters | BigDecimal | Requerido, @Positive, > 0 | Agua consumida en litros |
| mode | Enum | Requerido, DEFAULT AUTOMATIC | Forma de activacion |
| greenhouse_id | Long | FK -> Greenhouse(id), NOT NULL | Invernadero regado |

**Relaciones:** ManyToOne -> Greenhouse

---

## 6. Alert (alerta)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador de alerta |
| severity | Enum | Requerido, DEFAULT INFO | Nivel de alerta |
| message | String | Requerido, @NotBlank | Mensaje para el usuario |
| resolved | Boolean | Requerido, DEFAULT FALSE | Indica si fue atendida |
| createdAt | LocalDateTime | Requerido, DEFAULT CURRENT_TIMESTAMP | Fecha de creacion |
| sensor_id | Long | FK -> Sensor(id) | Sensor asociado |

**Relaciones:** ManyToOne -> Sensor

---

## 7. Actuator (actuador)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del actuador |
| name | String | Requerido, @NotBlank | Nombre del dispositivo |
| type | Enum | Requerido, DEFAULT IRRIGATION | Tipo de actuador |
| enabled | Boolean | Requerido, DEFAULT FALSE | Estado habilitado |
| active | Boolean | Requerido, DEFAULT TRUE | Estado activo |
| greenhouse_id | Long | FK -> Greenhouse(id) | Invernadero asociado |

**Relaciones:** ManyToOne -> Greenhouse

---

## 8. AutomationRule (regla de automatizacion)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador de la regla |
| name | String | Requerido, @NotBlank | Nombre de la regla |
| type | Enum | Requerido, DEFAULT LOW_HUMIDITY_IRRIGATION | Tipo de regla |
| threshold | BigDecimal | Opcional | Umbral de activacion |
| enabled | Boolean | Requerido, DEFAULT TRUE | Estado de la regla |
| greenhouse_id | Long | FK -> Greenhouse(id) | Invernadero asociado |

**Relaciones:** ManyToOne -> Greenhouse

---

## 9. Zone (zona)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador de la zona |
| name | String | Requerido, @NotBlank | Nombre de la zona |
| description | String | Opcional | Descripcion de la zona |
| active | Boolean | Requerido, DEFAULT TRUE | Estado activo |
| greenhouse_id | Long | FK -> Greenhouse(id) | Invernadero asociado |

**Relaciones:** ManyToOne -> Greenhouse; OneToMany -> Sensor

---

## 10. AuditLog (log de auditoria)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del log |
| action | String | Requerido, @NotBlank | Accion realizada |
| detail | String | Opcional | Detalle adicional |
| createdAt | LocalDateTime | Requerido, DEFAULT CURRENT_TIMESTAMP | Fecha de creacion |
| origin | Enum | Requerido, DEFAULT MANUAL | Origen de la accion |

**Relaciones:** Ninguna (tabla independiente)

---

## 11. AppUser (usuario de aplicacion)

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del usuario |
| email | String | Unico, requerido, @Email @NotBlank | Correo usado para autenticacion |
| fullName | String | Requerido, @NotBlank | Nombre completo |
| passwordHash | String | Requerido, @NotBlank | Hash de contrasena |
| provider | String | Requerido, DEFAULT 'email' | Proveedor de autenticacion |
| role | Enum | Requerido, DEFAULT VIEWER | Perfil de autorizacion |
| verified | Boolean | Requerido, DEFAULT FALSE | Email verificado |

**Relaciones:** Ninguna (tabla independiente)

---

## Enums del Sistema

| Enum | Valores | Descripcion |
|------|---------|-------------|
| ActionOrigin | MANUAL, AUTOMATIC | Origen de una accion |
| ActuatorType | IRRIGATION, FAN, HEATER, LIGHT | Tipo de actuador |
| AlertSeverity | INFO, WARNING, CRITICAL | Severidad de alerta |
| CropStatus | GERMINATING, GROWING, HARVESTED, LOST | Estado del cultivo |
| IrrigationMode | MANUAL, AUTOMATIC | Modo de riego |
| RuleType | LOW_HUMIDITY_IRRIGATION | Tipo de regla de automatizacion |
| SensorType | TEMPERATURE, HUMIDITY, SOIL_MOISTURE, LIGHT | Tipo de sensor |
| UserRole | ADMIN, OPERATOR, VIEWER | Rol de usuario |
