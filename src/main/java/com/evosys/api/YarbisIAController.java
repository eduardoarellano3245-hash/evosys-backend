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
    public ResponseEntity<Map<String, Object>> consultarIA(@RequestBody Map<String, Object> body) {
        Map<String, Object> respuesta = new HashMap<>();

        try {
            String mensaje = String.valueOf(body.getOrDefault("mensaje", "")).trim();
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

            String input = """
                    Eres YARBIS, asistente inteligente dentro de EVOSYS.
                    Responde en español mexicano, natural y breve.
                    Usa el contexto del sistema cuando aplique.
                    No inventes datos.

                    Mensaje del usuario:
                    %s

                    Contexto EVOSYS:
                    %s
                    """.formatted(mensaje, contexto);

            Map<String, Object> request = new HashMap<>();
            request.put("model", "gpt-4o-mini");
            request.put("input", input);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> openaiResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/responses",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map responseBody = openaiResponse.getBody();

            String texto = extraerTextoOpenAI(responseBody);

            if (texto == null || texto.isBlank()) {
                texto = "OpenAI respondió, pero no pude leer la respuesta.";
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
        if (responseBody == null) {
            return "";
        }

        Object outputText = responseBody.get("output_text");
        if (outputText != null && !String.valueOf(outputText).isBlank()) {
            return String.valueOf(outputText);
        }

        Object output = responseBody.get("output");

        if (!(output instanceof List<?> outputList)) {
            return "";
        }

        StringBuilder texto = new StringBuilder();

        for (Object itemObj : outputList) {
            if (!(itemObj instanceof Map<?, ?> item)) {
                continue;
            }

            Object content = item.get("content");

            if (!(content instanceof List<?> contentList)) {
                continue;
            }

            for (Object contentObj : contentList) {
                if (!(contentObj instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object text = contentMap.get("text");

                if (text != null && !String.valueOf(text).isBlank()) {
                    texto.append(String.valueOf(text)).append(" ");
                }
            }
        }

        return texto.toString().trim();
    }
}
