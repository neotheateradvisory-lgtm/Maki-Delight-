# Maki Delight

Aplicación web de pedidos construida con Spring Boot, Thymeleaf, Spring Data JPA y MySQL.

## Arquitectura

- `model`: entidades JPA que representan las tablas.
- `repository`: interfaces Spring Data para consultar y guardar entidades.
- `service`: reglas de negocio para usuarios, productos y pedidos.
- `controller`: páginas y operaciones HTTP.
- `src/main/resources/templates`: páginas Thymeleaf.

## Preparar MySQL

1. Abre `makidelight.sql` en MySQL Workbench y ejecútalo. El script crea la base `makidelight`, sus tablas y el catálogo inicial.
2. Configura las credenciales de MySQL mediante variables de entorno. En PowerShell:

   ```powershell
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "tu-contraseña"
   ```

   Si usas otra conexión, también puedes configurar `DB_URL`; por defecto es `jdbc:mysql://localhost:3306/makidelight`.
3. Inicia el proyecto desde esta carpeta:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Hibernate valida el esquema existente y no crea ni modifica tablas automáticamente.

## Usuarios y administración

Los registros nuevos se guardan como clientes; las contraseñas se almacenan con BCrypt. Para habilitar una cuenta administradora, registra primero la cuenta y luego cambia su rol en MySQL Workbench:

```sql
USE makidelight;
UPDATE usuarios SET rol = 'ADMIN' WHERE correo = 'tu-correo@ejemplo.com';
```

Después, vuelve a iniciar sesión. El panel `/dashboard` y sus operaciones están restringidos a usuarios con rol `ADMIN`.

Los pedidos invitados también se guardan en la base de datos. La página “Mis pedidos” muestra los pedidos vinculados a la cuenta que inició sesión.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan una base H2 aislada; no necesitan las credenciales de MySQL.
