# Despliegue de Kasero en Render

Usar el Dockerfile de la raíz para servir Angular y Spring Boot juntos.
La base de datos está en Supabase y debe tener el esquema inicializado.

Variables del servicio (credenciales solo en Render):

- `SUPABASE_DB_URL`: JDBC del session pooler con `sslmode=require`.
- `SUPABASE_DB_USER`: usuario del pooler.
- `SUPABASE_DB_PASSWORD`: contraseña de PostgreSQL.
- `KASERO_DEMO_ENABLED=false`: no crea cuentas demo y bloquea su ingreso,
  aunque ya existan en la base. No elimina ni modifica usuarios existentes.
- `SPRING_SQL_INIT_MODE=never`: no vuelve a aplicar semillas a una base ya preparada.
- `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=60 -XX:+UseSerialGC`: limita el heap
  respecto de la memoria del contenedor.

El puerto se toma de `PORT`, suministrado por Render. La aplicación utiliza
sesiones en memoria: un reinicio requiere ingresar nuevamente.
