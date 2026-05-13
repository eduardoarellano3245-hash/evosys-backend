package com.evosys.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
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
            );

            Object contexto = body.get("contexto");

            String promptSistema = """
                    Eres YARBIS.
                    Un asistente inteligente empresarial dentro de EVOSYS.

                    Responde en español mexicano.
                    Sé natural.
                    Sé útil.
                    Sé breve pero inteligente.

                    Puedes responder:
                    - ventas
                    - inventario
                    - clientes
                    - stock
                    - sistema
                    - preguntas casuales

                    Usa el contexto recibido.
                    No inventes información.
                    """;

            Map<String, Object> request = new HashMap<>();

            request.put("model", "gpt-4.1-mini");

            List<Map<String, Object>> input = new ArrayList<>();

            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", promptSistema);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put(
                    "content",
                    "Mensaje: " + mensaje + "\n\nContexto:\n" + contexto
            );

            input.add(systemMsg);
            input.add(userMsg);

            request.put("input", input);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.setBearerAuth(openaiApiKey);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<Map> openaiResponse =
                    restTemplate.exchange(
                            "https://api.openai.com/v1/responses",
                            HttpMethod.POST,
                            entity,
                            Map.class
                    );

            Map responseBody = openaiResponse.getBody();

            String texto = "";

            if (responseBody != null &&
                    responseBody.get("output") instanceof List outputList &&
                    !outputList.isEmpty()) {

                Object firstObj = outputList.get(0);

                if (firstObj instanceof Map firstMap &&
                        firstMap.get("content") instanceof List contentList &&
                        !contentList.isEmpty()) {

                    Object contentObj = contentList.get(0);

                    if (contentObj instanceof Map contentMap &&
                            contentMap.get("text") != null) {

                        texto = String.valueOf(contentMap.get("text"));
                    }
                }
            }

            if (texto.isBlank()) {
                texto = "No pude generar respuesta.";
            }

            respuesta.put("ok", true);
            respuesta.put("respuesta", texto);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {

            e.printStackTrace();

            respuesta.put("ok", false);
            respuesta.put("respuesta", "Error conectando IA.");
            respuesta.put("error", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(respuesta);
        }
    }
}
