package demo.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class WhatsAppClient {

    private final RestClient restClient;

    public WhatsAppClient(@Value("${whatsapp.api-key:}") String apiKey,
                          @Value("${whatsapp.base-url:https://waba.360dialog.io}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("D360-API-KEY", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void sendText(String to, String body) {
        Map<String, Object> payload = Map.of(
                "to", to,
                "type", "text",
                "text", Map.of("body", body));
        restClient.post()
                .uri("/v1/messages")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
