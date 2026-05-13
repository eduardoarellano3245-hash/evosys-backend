package com.evosys.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/yarbis")
@CrossOrigin(origins = "*")
public class YarbisIAController {

    @Value("${OPENAI_API_KEY:}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/health")
    public Map<String, Object> health() {

        Map<String, Object> res = new HashMap<>();

        res.put("ok", true);
        res.put("servicio", "YARBIS IA");
        res.put("apiKey", openaiApiKey != null && !openaiApiKey.isBlank());

        return res;
    }

    @PostMapping("/ia")
    public ResponseEntity<Map<String, Object>> consultarIA(
            @RequestBody Map<String, Object> body
    ) {

        Map<String, Object> respuesta = new HashMap<>();

        try {

            String mensaje = String.valueOf(
                    body.getOrDefault("mensaje", "")
            ).trim();

            Object contexto = body.get("contexto");

            if (mensaje.isBlank()) {

                respuesta.put("ok", false);
                respuesta.put("respuesta", "No recibí mensaje.");

                return ResponseEntity.ok(respuesta);
            }

            if (openaiApiKey == null || openaiApiKey.isBlank()) {

                respuesta.put("ok", false);
                respuesta.put("respuesta", "No tengo configurada la API KEY.");

                return ResponseEntity.ok(respuesta);
            }

            String prompt = """
                    Eres YARBIS, un asistente inteligente dentro de EVOSYS.

                    Responde:
                    - natural
                    - breve
                    - útil
                    - en español mexicano

                    Puedes hablar de:
                    - ventas
                    - inventario
                    - clientes
                    - productos
                    - sistema
                    - preguntas casuales

                    Usa el contexto cuando sea útil.
                    No inventes datos.

                    MENSAJE:
                    %s

                    CONTEXTO:
                    %s
                    """.formatted(mensaje, contexto);

            Map<String, Object> request = new HashMap<>();

            request.put("model", "gpt-3.5-turbo");

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put(
                    "content",
                    "Eres YARBIS, asistente inteligente de EVOSYS."
            );

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            messages.add(systemMessage);
            messages.add(userMessage);

            request.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.setBearerAuth(openaiApiKey);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<Map> openaiResponse =
                    restTemplate.exchange(
                            "https://api.openai.com/v1/chat/completions",
                            HttpMethod.POST,
                            entity,
                            Map.class
                    );

            Map responseBody = openaiResponse.getBody();

            String texto = extraerTextoOpenAI(responseBody);

            if (texto == null || texto.isBlank()) {
                texto = "La IA respondió vacía.";
            }

            respuesta.put("ok", true);
            respuesta.put("respuesta", texto);

            return ResponseEntity.ok(respuesta);

        } catch (HttpStatusCodeException e) {

            respuesta.put("ok", false);
            respuesta.put("respuesta", "OpenAI rechazó la solicitud.");
            respuesta.put("error", e.getResponseBodyAsString());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {

            respuesta.put("ok", false);
            respuesta.put("respuesta", "Error conectando con la IA.");
            respuesta.put("error", e.getMessage());

            return ResponseEntity.ok(respuesta);
        }
    }

    private String extraerTextoOpenAI(Map responseBody) {

        try {

            List choices = (List) responseBody.get("choices");

            if (choices == null || choices.isEmpty()) {
                return "";
            }

            Map firstChoice = (Map) choices.get(0);

            Map message = (Map) firstChoice.get("message");

            if (message == null) {
                return "";
            }

            Object content = message.get("content");

            return content != null
                    ? String.valueOf(content)
                    : "";

        } catch (Exception e) {
            return "";
        }
    }
}
