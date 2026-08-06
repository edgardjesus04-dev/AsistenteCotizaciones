package demo.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PricingTools {

    private final List<Service> services = List.of(
            new Service("plomeria", 350.0, List.of("plomer", "grifo", "tuber", "fuga", "fontaner", "cañer", "desague")),
            new Service("electricidad", 400.0, List.of("electric", "luz", "enchufe", "cable", "corto")),
            new Service("pintura", 45.0, List.of("pint", "pared", "muro", "fachada")));

    @Tool(name = "buscarPrecio", description = "Busca el precio unitario de un servicio por su nombre o descripcion")
    public Double buscarPrecio(String servicio) {
        String texto = servicio.toLowerCase();
        return services.stream()
                .filter(s -> s.palabrasClave().stream().anyMatch(texto::contains))
                .findFirst()
                .map(Service::precio)
                .orElse(null);
    }

    record Service(String nombre, Double precio, List<String> palabrasClave) {}
}
