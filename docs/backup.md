# Backup y Recuperación

Este documento describe los procedimientos mínimos de backup para PostgreSQL en el proyecto GreenHouse.

## Backup de la Base de Datos

### Local (PostgreSQL instalado en el sistema)

```bash
# Backup completo
pg_dump -U postgres -d greenhouse -F c -f greenhouse_backup_$(date +%Y%m%d).dump

# Backup solo datos (sin esquema)
pg_dump -U postgres -d greenhouse --data-only -f greenhouse_data_$(date +%Y%m%d).sql

# Backup solo esquema
pg_dump -U postgres -d greenhouse --schema-only -f greenhouse_schema_$(date +%Y%m%d).sql
```

> Nota: Se pedirá la contraseña del usuario postgres.

### Docker Compose

```bash
# Backup desde el contenedor PostgreSQL
docker exec greenhouse-postgres pg_dump -U greenhouse_user -d greenhouse -F c -f /tmp/backup.dump
docker cp greenhouse-postgres:/tmp/backup.dump ./greenhouse_backup_$(date +%Y%m%d).dump

# O en un solo comando
docker exec greenhouse-postgres pg_dump -U greenhouse_user -d greenhouse --data-only > greenhouse_backup_$(date +%Y%m%d).sql
```

### Neon (Cloud)

Neon realiza backups automáticos con Point-in-Time Recovery (PITR) de 7 días.

Para exportar manualmente:
```bash
pg_dump "postgresql://user:password@host.neon.tech/greenhouse?sslmode=require" -F c -f neon_backup.dump
```

---

## Restauración

### Local

```bash
# Restaurar backup completo
pg_restore -U postgres -d greenhouse -c greenhouse_backup_YYYYMMDD.dump

# Restaurar SQL plano
psql -U postgres -d greenhouse < greenhouse_backup_YYYYMMDD.sql
```

### Docker Compose

```bash
# Copiar backup al contenedor
docker cp greenhouse_backup_YYYYMMDD.dump greenhouse-postgres:/tmp/backup.dump

# Restaurar
docker exec greenhouse-postgres pg_restore -U greenhouse_user -d greenhouse -c /tmp/backup.dump
```

---

## Backup automático simple (cron)

Agregar a crontab (`crontab -e`):

```bash
# Backup diario a las 3:00 AM
0 3 * * * pg_dump -U postgres -d greenhouse -F c -f /backups/greenhouse_$(date +\%Y\%m\%d).dump 2>/dev/null

# Mantener solo los últimos 7 días
0 4 * * * find /backups -name "greenhouse_*.dump" -mtime +7 -delete
```

---

## Volúmenes Docker

El volumen `greenhouse_postgres_data` persiste datos entre reinicios:

```bash
# Listar volúmenes
docker volume ls

# Backup del volumen
docker run --rm -v greenhouse_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_data.tar.gz -C /data .

# Restaurar volumen
docker run --rm -v greenhouse_postgres_data:/data -v $(pwd):/backup alpine sh -c "cd /data && tar xzf /backup/postgres_data.tar.gz"
```
