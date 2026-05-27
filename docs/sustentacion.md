# Sustentacion — GreenHouse Manager

## Guia de presentacion para sustentacion final

---

## 1. Flujo de demo (15 min)

| Tiempo | Que mostrar | Que decir |
|--------|------------|-----------|
| 2 min | Introduccion + Arquitectura | Explicar stack: Spring Boot + React + Flask IA + PostgreSQL |
| 2 min | Login + Dashboard | Mostrar login, JWT, dashboard con metricas |
| 2 min | CRUD Invernadero | Crear un invernadero, agregar cultivo, sensor, riego |
| 2 min | Alertas + IoT | Mostrar alertas abiertas, ejecutar simulador IoT, ver lecturas |
| 2 min | IA Dashboard | Entrar a seccion IA, mostrar predicciones y recomendaciones |
| 2 min | Swagger + Testing | Abrir Swagger, mostrar endpoints, mostrar GitHub Actions verde |
| 2 min | Dark mode + i18n | Cambiar a ingles, activar dark mode, mostrar internacionalizacion |
| 1 min | Cierre | Resumir logros, tecnologias, arquitectura |

---

## 2. Preguntas frecuentes del jurado

### Arquitectura y decisiones tecnicas

**P: ¿Por que Spring Boot y no otro framework?**
R: Spring Boot 3 ofrece seguridad integrada (Spring Security), ORM maduro (JPA/Hibernate), migraciones (Flyway), y documentacion automatica (Swagger). Es el estandar empresarial Java.

**P: ¿Por que JWT en vez de sesiones?**
R: JWT permite autenticacion stateless, ideal para APIs REST. El frontend guarda el token y lo envia en cada request. No requiere session en servidor, escala horizontalmente.

**P: Diferencias con otros proyectos del curso**
R: Nuestro proyecto usa **JPA relacional real** (12 entidades con `@OneToMany`, `@ManyToOne`, cascadas, FetchType.LAZY). Otros proyectos usan Long como FK sin relaciones reales. Esto nos da integridad referencial a nivel de base de datos.

### Seguridad

**P: ¿Como funciona la seguridad?**
R: Doble capa: (1) `SecurityConfig` con reglas por URL y metodo HTTP, (2) `@PreAuthorize` a nivel de metodo. JWT con BCrypt para passwords. OAuth2 Google como alternativa.

**P: ¿Que pasa si el token expira?**
R: El frontend detecta 401, intenta refrescar automaticamente via `/api/auth/refresh`. Si falla, redirige al login.

### IA

**P: ¿Que tipo de IA implementaron?**
R: Regresion lineal multiple con scikit-learn. Entrena con datos historicos de temperatura y humedad para predecir valores futuros. Incluye deteccion de anomalias via Z-score y recomendaciones basadas en nivel de riesgo.

**P: ¿Por que Flask y no integrarlo en Spring Boot?**
R: Separacion de responsabilidades. Flask es liviano para microservicios de IA. Spring Boot consume Flask via REST. Esto permite escalar independientemente.

### IoT

**P: ¿Es IoT real o simulado?**
R: Simulado con Python. Genera lecturas periodicas con fluctuaciones normales y anomalias aleatorias. Envia datos al backend via REST. La arquitectura permite reemplazar el simulador por sensores reales sin cambios en el backend.

### DevOps

**P: ¿Que automatizacion tienen?**
R: GitHub Actions con 4 jobs: backend (JUnit), frontend (Vitest + ESLint + build), validacion JSON (Python), IA (pytest). Docker Compose levanta todo el ecosistema. Deploy-ready para Vercel + Railway + Neon.

---

## 3. Puntos a destacar en sustentacion

| Punto fuerte | Donde demostrarlo |
|-------------|------------------|
| JPA relacional real | `domain/Sensor.java` — `@ManyToOne`, `@OneToMany`, cascade |
| DTOs separados | 22 DTOs en `web/dto/` — separacion Request/Response |
| Seguridad doble capa | `SecurityConfig.java` + `@PreAuthorize` en cada controller |
| Testing multiple | JUnit (16), Vitest (4), pytest (8), Selenium (4) |
| CI/CD completo | GitHub Actions — 4 jobs, cache, lint |
| IA predictiva | Flask + scikit-learn — regresion lineal |
| IoT simulado | Python — sensores, anomalias, alertas |
| Docker | `docker compose up --build` — todo el sistema |
| Swagger | 30+ endpoints documentados con JWT Bearer |
| i18n | Frontend: 140+ claves ES/EN. Backend: 32 mensajes |

---

## 4. Evidencia a mostrar obligatoriamente

- [ ] Swagger UI en `/swagger-ui.html`
- [ ] GitHub Actions con 4 jobs verdes
- [ ] Docker Compose funcionando
- [ ] Login JWT + OAuth2 Google
- [ ] Dashboard con metricas reales
- [ ] Dashboard IA con predicciones
- [ ] IoT Simulator generando lecturas
- [ ] Dark mode / i18n / Responsive
- [ ] Diagrama entidad-relacion
- [ ] 16 tests JUnit pasando
- [ ] Pip install + pytest IA
- [ ] Codigo fuente (JPA relations, DTOs)

---

## 5. Checklist pre-sustentacion

- [ ] Docker Compose funciona sin errores
- [ ] Backend inicia con `mvn spring-boot:run`
- [ ] Frontend inicia con `npm run dev`
- [ ] Swagger accesible
- [ ] Login funciona (admin@greenhouse.local / admin1234)
- [ ] Flask IA responde en puerto 5000
- [ ] IoT simulator envia lecturas
- [ ] GitHub Actions verde
- [ ] README.md con badges ok
- [ ] Docs actualizados
- [ ] Screenshots capturados
- [ ] Presentacion lista
