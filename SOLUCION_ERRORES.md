# 🚀 GUÍA DE INSTALACIÓN Y SOLUCIÓN DE PROBLEMAS

## ❌ Errores Actuales

Los errores que ves son **NORMALES** porque las dependencias de Spring Boot aún no están instaladas. Los errores típicos son:

```
- The import org.springframework cannot be resolved
- SpringBootApplication cannot be resolved to a type
- JpaRepository cannot be resolved to a type
- Entity cannot be resolved to a type
```

## ✅ SOLUCIÓN: Instalar Dependencias

### Opción 1: Usando el Script Automático (Recomendado)

1. **Abre PowerShell o CMD** en la carpeta del proyecto
2. **Ejecuta el script de instalación:**
   ```cmd
   .\install.bat
   ```
3. Espera a que Maven descargue todas las dependencias (puede tardar 2-5 minutos la primera vez)

### Opción 2: Manual con Maven

1. **Verifica que tienes Maven instalado:**
   ```cmd
   mvn --version
   ```
   
   Si no está instalado, descárgalo de: https://maven.apache.org/download.cgi

2. **Navega a la carpeta del proyecto:**
   ```cmd
   cd C:\Users\Usuario\Desktop\bot_futbol\backend\Bot_Futbol
   ```

3. **Ejecuta Maven para instalar dependencias:**
   ```cmd
   mvn clean install -DskipTests
   ```

### Opción 3: Desde VS Code

1. Abre la terminal integrada en VS Code (Ctrl + `)
2. Navega al proyecto:
   ```cmd
   cd backend\Bot_Futbol
   ```
3. Ejecuta:
   ```cmd
   mvn clean install -DskipTests
   ```

## 🔧 Verificar la Instalación

Después de instalar, los errores deberían desaparecer. Puedes verificar que todo funciona ejecutando:

```cmd
mvn spring-boot:run
```

O usando el script:
```cmd
.\run.bat
```

La aplicación debería iniciar en: **http://localhost:8080**

## 📦 ¿Qué se instaló?

El archivo `pom.xml` descarga automáticamente:

- ✅ Spring Boot 3.2.0
- ✅ Spring Data JPA (para bases de datos)
- ✅ Hibernate (ORM)
- ✅ H2 Database (base de datos en memoria)
- ✅ MySQL Connector
- ✅ PostgreSQL Driver
- ✅ Spring Web (para API REST)
- ✅ Validation
- ✅ DevTools

## 🐛 Problemas Comunes

### Error: "mvn no se reconoce"

**Solución:** Maven no está instalado o no está en el PATH.

1. Descarga Maven: https://maven.apache.org/download.cgi
2. Descomprime en `C:\Program Files\Apache\maven`
3. Agrega al PATH: `C:\Program Files\Apache\maven\bin`
4. Reinicia la terminal

### Error: "JAVA_HOME not set"

**Solución:** Java no está configurado correctamente.

1. Verifica que tienes Java 17+: `java -version`
2. Configura JAVA_HOME:
   ```cmd
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   ```
3. Reinicia la terminal

### Los errores NO desaparecen después de instalar

**Solución:** VS Code necesita refrescar el proyecto.

1. Presiona `Ctrl + Shift + P`
2. Busca: "Java: Clean Java Language Server Workspace"
3. Ejecuta el comando
4. Reinicia VS Code

### Error al compilar

**Solución:** Limpia y reinstala:

```cmd
mvn clean
mvn install -DskipTests
```

## 📊 Estado del Proyecto

### ✅ Completado

- Estructura de carpetas
- Entidades JPA (Player, Payment, Goal, Match, Team)
- DTOs para transferencia de datos
- Repositorios Spring Data JPA
- Servicios con lógica de negocio
- Controlador REST
- Configuración de base de datos (application.properties)
- Archivo pom.xml con todas las dependencias

### ⏳ Pendiente (se resuelve con la instalación)

- Descargar dependencias de Maven
- Compilar el proyecto
- Ejecutar la aplicación

## 🎯 Próximos Pasos

1. **Instalar dependencias** con `.\install.bat` o `mvn clean install`
2. **Verificar que no hay errores** en el código
3. **Ejecutar la aplicación** con `.\run.bat` o `mvn spring-boot:run`
4. **Probar la API** en http://localhost:8080
5. **Acceder a H2 Console** en http://localhost:8080/h2-console

## 💡 Comandos Útiles

```bash
# Compilar sin ejecutar tests
mvn clean install -DskipTests

# Ejecutar aplicación
mvn spring-boot:run

# Crear JAR ejecutable
mvn clean package

# Ejecutar el JAR
java -jar target/bot-futbol-1.0.0.jar

# Ver dependencias
mvn dependency:tree

# Actualizar proyecto VS Code
Ctrl + Shift + P -> "Java: Clean Java Language Server Workspace"
```

## 📞 ¿Necesitas Ayuda?

Si después de seguir estos pasos sigues teniendo problemas:

1. Comparte el mensaje de error completo
2. Verifica la versión de Java: `java -version`
3. Verifica la versión de Maven: `mvn --version`
4. Revisa el archivo `pom.xml` esté completo
