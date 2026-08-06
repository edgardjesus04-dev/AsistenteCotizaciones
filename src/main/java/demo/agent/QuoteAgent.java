package demo.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class QuoteAgent {

    private final ChatClient chatClient;

    public QuoteAgent(ChatClient.Builder builder, PricingTools tools, ChatMemory chatMemory) {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        this.chatClient = builder
                .defaultSystem("""
                        Eres un agente de cotizaciones de una empresa de mantenimiento del hogar.
                        Cuando te pidan un precio o cotizacion, SIEMPRE llama a la herramienta buscarPrecio
                        para obtener el precio unitario. Luego calcula el total (precio x cantidad) y
                        responde en espanol, de forma breve. Usa el contexto de la conversacion cuando
                        el usuario haga referencia a algo dicho antes.
                        """)
                .defaultTools(tools)
                .defaultAdvisors(memoryAdvisor)
                .build();
    }

    public String handle(String conversationId, String mensajeUsuario) {
        return chatClient.prompt()
                .user(mensajeUsuario)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
