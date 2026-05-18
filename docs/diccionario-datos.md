# Diccionario De Datos

## Greenhouse

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK, autoincremental | Identificador del invernadero |
| name | String | Requerido, maximo 120 | Nombre visible |
| location | String | Requerido, maximo 160 | Ubicacion fisica |
| areaSquareMeters | Decimal | Mayor que 0 | Area en metros cuadrados |
| active | Boolean | Requerido | Estado operativo |

## Crop

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK | Identificador del cultivo |
| name | String | Requerido | Nombre comun |
| variety | String | Opcional | Variedad o referencia agricola |
| plantedAt | Date | Requerido | Fecha de siembra |
| expectedHarvestAt | Date | Posterior a plantedAt | Fecha estimada de cosecha |
| status | Enum | GERMINATING, GROWING, HARVESTED, LOST | Estado del cultivo |
| greenhouse_id | Long | FK | Invernadero asociado |

## Sensor

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK | Identificador del sensor |
| code | String | Unico, requerido | Codigo fisico del sensor |
| type | Enum | TEMPERATURE, HUMIDITY, SOIL_MOISTURE, LIGHT | Tipo de medicion |
| unit | String | Requerido | Unidad de medida |
| minThreshold | Decimal | Opcional | Limite inferior aceptable |
| maxThreshold | Decimal | Opcional | Limite superior aceptable |
| greenhouse_id | Long | FK | Invernadero donde esta instalado |

## Reading

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK | Identificador de lectura |
| value | Decimal | Requerido | Valor medido |
| recordedAt | DateTime | Requerido | Fecha y hora de lectura |
| sensor_id | Long | FK | Sensor que genero la lectura |

## IrrigationEvent

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK | Identificador del riego |
| startedAt | DateTime | Requerido | Inicio del riego |
| durationMinutes | Integer | Mayor que 0 | Duracion |
| waterLiters | Decimal | Mayor que 0 | Agua consumida |
| mode | Enum | MANUAL, AUTOMATIC | Forma de activacion |
| greenhouse_id | Long | FK | Invernadero regado |

## Alert

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK | Identificador de alerta |
| severity | Enum | INFO, WARNING, CRITICAL | Nivel de alerta |
| message | String | Requerido | Mensaje para el usuario |
| resolved | Boolean | Requerido | Indica si fue atendida |
| createdAt | DateTime | Requerido | Fecha de creacion |
| sensor_id | Long | FK | Sensor asociado |

## AppUser

| Campo | Tipo | Reglas | Descripcion |
| --- | --- | --- | --- |
| id | Long | PK | Identificador del usuario |
| email | String | Unico, formato email | Correo usado para autenticacion |
| fullName | String | Requerido | Nombre completo |
| role | Enum | ADMIN, OPERATOR, VIEWER | Perfil de autorizacion |
