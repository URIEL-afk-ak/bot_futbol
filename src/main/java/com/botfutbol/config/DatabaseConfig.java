package com.botfutbol.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.net.URI;

/**
 * Configuración de base de datos para Supabase/Render.
 * Parsea DATABASE_URL si está disponible, de lo contrario usa variables individuales.
 */
@Configuration
@Profile("production")
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
                
                URI dbUri = new URI(urlToParse.replace("postgresql://", "http://").replace("postgres://", "http://"));
                
                String userInfo = dbUri.getUserInfo();
                if (userInfo == null || !userInfo.contains(":")) {
                    throw new IllegalArgumentException("DATABASE_URL debe incluir usuario y contraseña: user:password@host");
                }
                
                username = userInfo.split(":")[0];
                password = userInfo.split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
                String database = dbUri.getPath().replaceFirst("/", "");
                
                if (database.isEmpty()) {
                    database = "postgres";
                }
                
                // Construir JDBC URL con parámetros SSL para Supabase
                // Usar 'prefer' para mayor compatibilidad (intenta SSL pero permite sin SSL)
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=prefer&connectTimeout=10", host, port, database);
                
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
        
        // Prioridad 3: application.properties (solo para desarrollo local)
        if (jdbcUrl == null) {
            logger.warn("⚠️ No se encontraron variables de entorno (DATABASE_URL o DB_*). Usando configuración de application.properties (localhost:3001)");
            logger.warn("⚠️ Esto solo funciona en desarrollo local. Para producción, configura DATABASE_URL en Render.");
            logger.warn("⚠️ DATABASE_URL está vacía o no configurada. Valor actual: '{}'", databaseUrl != null ? "no vacía" : "null");
            logger.warn("⚠️ DB_HOST: '{}', DB_NAME: '{}', DB_USERNAME: '{}'", dbHost, dbName, dbUsername);
            
            // Usar valores por defecto de application.properties
            DataSourceProperties defaultProperties = new DataSourceProperties();
            jdbcUrl = defaultProperties.getUrl();
            username = defaultProperties.getUsername();
            password = defaultProperties.getPassword();
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
        
        return new HikariDataSource(config);
    }
}

