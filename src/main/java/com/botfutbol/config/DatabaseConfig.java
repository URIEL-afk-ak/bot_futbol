package com.botfutbol.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


/**
 * Configuración de base de datos para Supabase/Render.
 * Se activa únicamente cuando DATABASE_URL está definida (producción).
 * En desarrollo usa el datasource por defecto de application.properties.
 */
@Configuration
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Value("${DATABASE_URL:}")
    private String databaseUrl;
    
    @Value("${DB_HOST:}")
    private String dbHost;
    
    @Value("${DB_PORT:5432}")
    private String dbPort;
    
    @Value("${DB_NAME:}")
    private String dbName;
    
    @Value("${DB_USERNAME:}")
    private String dbUsername;
    
    @Value("${DB_PASSWORD:}")
    private String dbPassword;
    
    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource dataSource() {
        String jdbcUrl;
        String username;
        String password;
        
        // Prioridad 1: DATABASE_URL (formato Supabase/Render: postgresql://user:pass@host:port/dbname)
        if (databaseUrl != null && !databaseUrl.isEmpty() && !databaseUrl.startsWith("jdbc:")) {
            try {
                logger.info("🔍 Intentando parsear DATABASE_URL (primeros 50 caracteres): {}", 
                    databaseUrl.length() > 50 ? databaseUrl.substring(0, 50) + "..." : databaseUrl);
                
                // Manejar URLs con o sin esquema
                String urlToParse = databaseUrl;
                if (!urlToParse.startsWith("postgresql://") && !urlToParse.startsWith("postgres://")) {
                    urlToParse = "postgresql://" + urlToParse;
                }

                // Remover esquema
                String withoutScheme = urlToParse
                    .replaceFirst("^postgresql://", "")
                    .replaceFirst("^postgres://", "");

                // Separar credenciales del host usando lastIndexOf('@')
                int atIndex = withoutScheme.lastIndexOf('@');
                if (atIndex == -1) {
                    throw new IllegalArgumentException("DATABASE_URL debe incluir usuario y contraseña: user:password@host");
                }
                String userInfo = withoutScheme.substring(0, atIndex);
                String hostPart = withoutScheme.substring(atIndex + 1);

                // Separar usuario de contraseña usando indexOf(':') (primer ':')
                int colonInUser = userInfo.indexOf(':');
                if (colonInUser == -1) {
                    throw new IllegalArgumentException("DATABASE_URL debe incluir contraseña: user:password@host");
                }
                username = userInfo.substring(0, colonInUser);
                password = userInfo.substring(colonInUser + 1);

                // Separar host:port/database
                int slashIndex = hostPart.indexOf('/');
                String hostAndPort = slashIndex == -1 ? hostPart : hostPart.substring(0, slashIndex);
                String database = slashIndex == -1 ? "postgres" : hostPart.substring(slashIndex + 1);
                if (database.isEmpty()) database = "postgres";

                // Separar host de port
                int portColon = hostAndPort.lastIndexOf(':');
                String host;
                int port;
                if (portColon == -1) {
                    host = hostAndPort;
                    port = 5432;
                } else {
                    host = hostAndPort.substring(0, portColon);
                    port = Integer.parseInt(hostAndPort.substring(portColon + 1));
                }

                // Construir JDBC URL con SSL requerido para Supabase
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require&connectTimeout=10", host, port, database);
                
                logger.info("✅ Configuración de base de datos desde DATABASE_URL:");
                logger.info("   Usuario: {}", username);
                logger.info("   Host: {}", host);
                logger.info("   Puerto: {}", port);
                logger.info("   Base de datos: {}", database);
                logger.info("   JDBC URL: jdbc:postgresql://{}:{}/{}", host, port, database);
            } catch (Exception e) {
                logger.error("❌ Error al parsear DATABASE_URL: {}", e.getMessage());
                logger.error("   DATABASE_URL recibida: {}", databaseUrl);
                e.printStackTrace();
                // Continuar con variables individuales
                jdbcUrl = null;
                username = null;
                password = null;
            }
        } else {
            if (databaseUrl == null || databaseUrl.isEmpty()) {
                logger.warn("⚠️ DATABASE_URL está vacía o no configurada");
            } else {
                logger.warn("⚠️ DATABASE_URL parece ser una URL JDBC directa: {}", databaseUrl.substring(0, Math.min(30, databaseUrl.length())));
            }
            jdbcUrl = null;
            username = null;
            password = null;
        }
        
        // Prioridad 2: Variables individuales (DB_HOST, DB_PORT, etc.)
        if (jdbcUrl == null && dbHost != null && !dbHost.isEmpty() && 
            dbName != null && !dbName.isEmpty() && 
            dbUsername != null && !dbUsername.isEmpty() && 
            dbPassword != null && !dbPassword.isEmpty()) {
            
            try {
                int port = Integer.parseInt(dbPort);
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", dbHost, port, dbName);
                username = dbUsername;
                password = dbPassword;
                
                logger.info("✅ Configuración de base de datos desde variables individuales: {}@{}:{}/{}", 
                    username, dbHost, port, dbName);
            } catch (Exception e) {
                logger.error("❌ Error al configurar desde variables individuales: {}", e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Sin configuración válida → error claro en lugar de NPE críptico
        if (jdbcUrl == null) {
            String msg = "❌ No se encontró configuración de base de datos para producción.\n" +
                         "   Configura en el dashboard de Render la variable DATABASE_URL con el formato:\n" +
                         "   postgresql://usuario:contraseña@host:puerto/base_de_datos\n" +
                         "   (o las variables individuales DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD)";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        
        logger.info("🔌 Creando DataSource para: {}@{}", username, jdbcUrl != null ? jdbcUrl.replaceAll("jdbc:postgresql://", "").split("/")[0] : "unknown");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setConnectionInitSql("SELECT 1");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setInitializationFailTimeout(60000);

        try {
            HikariDataSource ds = new HikariDataSource(config);
            logger.info("✅ Conexión a base de datos establecida correctamente");
            return ds;
        } catch (Exception e) {
            logger.error("❌ Error al crear la conexión a la base de datos: {}", e.getMessage());
            logger.error("   JDBC URL usada: {}", jdbcUrl);
            logger.error("   Usuario: {}", username);
            throw new RuntimeException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }
}

