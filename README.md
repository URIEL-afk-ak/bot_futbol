# Bot de Fútbol ⚽

Sistema de gestión de partidos de fútbol amateur con Spring Boot.

## 🚀 Características

- ✅ Administración de jugadores
- ✅ Generación de equipos (random/balanceados)
- ✅ Registro de pagos y control de deudas
- ✅ Registro de goles y estadísticas
- ✅ API REST completa
- ✅ Base de datos H2/MySQL/PostgreSQL
- ✅ Persistencia con JPA/Hibernate

## 📋 Requisitos

- Java 17 o superior
- Maven 3.6+
- MySQL 8.0+ (opcional, usa H2 por defecto)

## ⚙️ Configuración

### Configuración de Base de Datos

Por defecto, la aplicación usa H2 en memoria. Para cambiar a MySQL o PostgreSQL, edita `src/main/resources/application.properties`:

**MySQL:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bot_futbol?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=tu_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

**PostgreSQL:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bot_futbol
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## 🏃‍♂️ Ejecutar la Aplicación

### Con Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Con Java

```bash
mvn clean package
java -jar target/bot-futbol-1.0.0.jar
```

## 🌐 Acceso

- **API REST**: http://localhost:8080/api/bot
- **H2 Console**: http://localhost:8080/h2-console

### Credenciales H2 Console
- JDBC URL: `jdbc:h2:mem:botfutbol`
- Username: `sa`
- Password: *(vacío)*

## 📡 API Endpoints

### Jugadores

```
POST   /api/bot/message          - Procesar comando de texto
GET    /api/players               - Obtener todos los jugadores
POST   /api/players               - Agregar jugador
DELETE /api/players/{name}        - Eliminar jugador
```

### Equipos

```
GET    /api/teams/generate        - Generar equipos random
GET    /api/teams/balanced        - Generar equipos balanceados
```

### Partidos

```
POST   /api/match/start           - Iniciar partido
POST   /api/match/goal            - Registrar gol
GET    /api/match/score           - Obtener marcador
POST   /api/match/end             - Finalizar partido
```

### Pagos

```
POST   /api/payments              - Registrar pago
GET    /api/payments/debts        - Ver deudores
GET    /api/payments/player/{name} - Ver pagos de jugador
```

### Estadísticas

```
GET    /api/stats                 - Obtener estadísticas generales
```

## 💬 Comandos de Texto

Puedes enviar comandos de texto al endpoint `/api/bot/message`:

```json
{
  "message": "/agregar Juan 8"
}
```

### Lista de Comandos

**Jugadores:**
- `/agregar <nombre> [nivel]` - Agregar jugador
- `/eliminar <nombre>` - Eliminar jugador
- `/lista` - Ver todos los jugadores

**Partido:**
- `/equipos [balanceado]` - Generar equipos
- `/iniciar <costo>` - Iniciar partido
- `/gol <jugador> <A|B>` - Registrar gol
- `/resultado` - Ver marcador
- `/finalizar` - Finalizar partido

**Pagos:**
- `/pago <jugador> <monto>` - Registrar pago
- `/deuda [jugador]` - Ver deudas

**Otros:**
- `/stats` - Ver estadísticas
- `/ayuda` - Mostrar ayuda

## 🏗️ Arquitectura

El proyecto sigue una arquitectura en capas bien definida:

```
📁 entity/       - Entidades del dominio (Player, Team, Goal, Payment, Match)
📁 dto/          - Objetos de transferencia de datos
📁 repository/   - Interfaces de repositorios (Spring Data JPA)
📁 service/      - Lógica de negocio
📁 controller/   - Controladores REST
```

### Responsabilidades por Capa

- **Entity**: Modelo de datos, mapeo JPA
- **DTO**: Transferencia de datos entre capas
- **Repository**: Acceso a datos (CRUD)
- **Service**: Lógica de negocio, validaciones
- **Controller**: Manejo de peticiones HTTP, validación de entrada

## 🗄️ Modelo de Datos

### Player
- ID, nombre, nivel de habilidad
- Deuda total, total pagado
- Partidos jugados, goles anotados

### Payment
- ID, jugador, monto
- Fecha, concepto

### Goal
- ID, jugador, equipo
- Partido, timestamp

### Match
- ID, fecha, equipos
- Costo por jugador, estado activo

## 🧪 Testing

```bash
mvn test
```

## 📝 Perfiles de Ejecución

- **default**: H2 en memoria (desarrollo)
- **prod**: MySQL en producción
- **test**: H2 para pruebas

```bash
# Ejecutar con perfil de producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🛠️ Tecnologías

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Hibernate
- H2 Database
- MySQL Connector
- PostgreSQL Driver
- Maven

## 📄 Licencia

MIT

## 👥 Autor

Bot de Fútbol - Sistema de gestión amateur
