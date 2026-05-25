# Mockups — GreenHouse Manager

Capturas de pantalla del sistema funcionando, organizadas para sustentacion.

## Pantallas

| # | Screenshot | Funcionalidad | Relacion rubrica |
|---|-----------|--------------|-----------------|
| 1 | Login | Pantalla de inicio de sesion con email y Google OAuth | 6. OAuth + 9. Frontend |
| 2 | Dashboard | Dashboard principal con metricas, graficos y alertas | 6. Frontend + 12. Dashboard |
| 3 | CRUD Invernaderos | Creacion, edicion y eliminacion de invernaderos | 6. Frontend + 5. Backend |
| 4 | Alertas | Gestion de alertas abiertas y resueltas | 6. Frontend + 12. Dashboard |
| 5 | Dark Mode | Interfaz en modo oscuro | 6. Frontend + UX |
| 6 | IA Dashboard | Predicciones y recomendaciones del modulo IA | 16. IA + 12. Dashboard |
| 7 | IoT Simulator | Simulador de sensores IoT corriendo en terminal | 17. IoT |
| 8 | Swagger UI | Documentacion interactiva de la API | 12. Swagger/AppDocs |
| 9 | GitHub Actions | Pipeline de CI/CD con todos los checks verdes | 8. CI/CD |
| 10 | Docker Compose | Contenedores corriendo: backend + frontend + postgres | 18. Despliegue |

## Como generar los screenshots

```powershell
# 1. Iniciar el sistema
docker compose up --build

# 2. Abrir frontend en http://localhost:5173

# 3. Tomar screenshots con Windows + Shift + S
# Guardar en docs/mockups/ con nombres descriptivos

# 4. Opcional: herramientas CLI
# Para IA dashboard: iniciar Flask en ia/ (python app.py)
# Para IoT: ejecutar python iot/simulador.py --interval 3
```

## Formato

- Formato: PNG
- Tamaño recomendado: 1920x1080
- Nombre: `NN-nombre-descripcion.png`
