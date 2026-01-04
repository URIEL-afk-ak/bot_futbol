package com.botfutbol.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Configuración de Firebase Admin SDK.
 */
@Configuration
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    
    @Value("${firebase.credentials.path:}")
    private String credentialsPath;
    
    @Value("${firebase.credentials.json:}")
    private String credentialsJson;
    
    @PostConstruct
    public void initialize() {
        try {
            // Si ya existe una instancia de FirebaseApp, no inicializar de nuevo
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials;
                
                // Prioridad 1: Usar JSON desde variable de entorno
                if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                    logger.info("Inicializando Firebase con credenciales desde variable de entorno");
                    InputStream serviceAccount = new ByteArrayInputStream(
                        credentialsJson.getBytes(StandardCharsets.UTF_8)
                    );
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }
                // Prioridad 2: Usar archivo
                else if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                    logger.info("Inicializando Firebase con credenciales desde archivo: {}", credentialsPath);
                    InputStream serviceAccount = new FileInputStream(credentialsPath);
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }
                // Sin credenciales
                else {
                    logger.warn("⚠️ No se configuraron credenciales de Firebase. Las notificaciones push no funcionarán.");
                    logger.warn("Configura FIREBASE_CREDENTIALS_JSON o firebase.credentials.path en application.properties");
                    return;
                }
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
                
                FirebaseApp.initializeApp(options);
                logger.info("✅ Firebase inicializado correctamente");
            }
        } catch (IOException e) {
            logger.error("❌ Error al inicializar Firebase: {}", e.getMessage());
            logger.warn("Las notificaciones push no estarán disponibles");
        }
    }
}

