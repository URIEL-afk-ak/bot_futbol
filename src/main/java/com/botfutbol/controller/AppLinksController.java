package com.botfutbol.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controlador para manejar Universal Links / App Links
 * Sirve las páginas de invitación y el archivo assetlinks.json
 */
@Controller
@RequestMapping
public class AppLinksController {

    /**
     * Maneja las invitaciones vía web
     * URL: https://botfutbol.app/invite/{code}
     * 
     * Si la app está instalada → abre la app (App Link)
     * Si NO está instalada → muestra landing page con botón de descarga
     */
    @GetMapping("/invite/{code}")
    public ResponseEntity<String> handleInvite(@PathVariable String code) throws IOException {
        // Cargar el HTML de invitación
        Resource resource = new ClassPathResource("static/invite.html");
        String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        
        // Reemplazar el placeholder con el código real
        html = html.replace("{{INVITATION_CODE}}", code);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Sirve el archivo assetlinks.json requerido por Android para verificar App Links
     * DEBE estar disponible en: https://botfutbol.app/.well-known/assetlinks.json
     * 
     * Este archivo verifica que tu app tiene permiso para abrir enlaces de este dominio
     */
    @GetMapping("/.well-known/assetlinks.json")
    @ResponseBody
    public ResponseEntity<String> getAssetLinks() throws IOException {
        Resource resource = new ClassPathResource("static/.well-known/assetlinks.json");
        String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .body(json);
    }

    /**
     * Endpoint de prueba para verificar que los App Links están funcionando
     */
    @GetMapping("/invite/test")
    @ResponseBody
    public ResponseEntity<String> testInvite() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body("✅ App Links endpoint funcionando correctamente\n\n" +
                      "Prueba con: https://botfutbol.app/invite/TEST123\n" +
                      "O desde la app: botfutbol://invite/TEST123");
    }
}
