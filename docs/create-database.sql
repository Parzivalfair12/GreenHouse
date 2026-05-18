-- Greenhouse Manager - PostgreSQL database bootstrap
-- Ejecutar conectado como usuario administrador de PostgreSQL, por ejemplo `postgres`.
--
-- Si usas Docker con `docker compose up -d`, este script NO es obligatorio:
-- Docker crea automaticamente la base `greenhouse` y el usuario `greenhouse_user`.
--
-- Usalo cuando quieras crear la base manualmente desde pgAdmin o psql.

CREATE USER greenhouse_user WITH PASSWORD 'greenhouse_pass';

CREATE DATABASE greenhouse
  WITH OWNER = greenhouse_user
  ENCODING = 'UTF8';

GRANT ALL PRIVILEGES ON DATABASE greenhouse TO greenhouse_user;
