package demo.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatDebugController {

    private final QuoteAgent agent;

    public ChatDebugController(QuoteAgent agent) {
        this.agent = agent;
    }

    @GetMapping
    public String chat(@RequestParam(value = "cid", defaultValue = "demo") String conversationId,
                       @RequestParam(value = "msg", defaultValue = "¿Cuánto cuesta pintar una casa de 100m2?") String msg) {
        return agent.handle(conversationId, msg);
    }
}
