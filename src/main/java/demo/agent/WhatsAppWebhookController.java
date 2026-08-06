package demo.agent;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/webhook")
public class WhatsAppWebhookController {

    private final QuoteAgent agent;
    private final WhatsAppClient whatsapp;

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    public WhatsAppWebhookController(QuoteAgent agent, WhatsAppClient whatsapp) {
        this.agent = agent;
        this.whatsapp = whatsapp;
    }

    @GetMapping
    public ResponseEntity<String> verify(@RequestParam(value = "hub.challenge", required = false) String challenge) {
        if (challenge != null) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.ok("quote-agent webhook activo");
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody JsonNode body) {
        JsonNode value = body.path("entry").path(0).path("changes").path(0).path("value");
        JsonNode messages = value.path("messages");
        if (messages.isArray() && !messages.isEmpty()) {
            JsonNode msg = messages.get(0);
            String from = msg.path("from").asText();
            String text = msg.path("text").path("body").asText();
            CompletableFuture.runAsync(() -> processMessage(from, text));
        }
        return ResponseEntity.ok().build();
    }

    private void processMessage(String from, String text) {
        try {
            String reply = agent.handle(from, text);
            log.info("Respondiendo a {}: {}", from, reply);
            whatsapp.sendText(from, reply);
        } catch (Exception e) {
            log.error("Error procesando mensaje de {}", from, e);
        }
    }
}
