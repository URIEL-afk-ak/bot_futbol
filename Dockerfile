# Etapa 1: Compilar la aplicación
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Crear imagen de ejecución
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar JAR compilado desde la etapa de build
COPY --from=build /app/target/bot-futbol-*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto (se sobreescriben en Render)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
