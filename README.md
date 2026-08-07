# Asistente de Cotizaciones

Agente de IA para cotizaciones de mantenimiento del hogar construido con **Java 17 + Spring Boot 3 + Spring AI**. Responde cotizaciones usando *function calling* (busca precios reales con una herramienta), mantiene **memoria conversacional** por usuario y puede conectarse a **WhatsApp** mediante la API de 360dialog.

## Funcionalidades

- Agente con `Function Calling`: llama a una herramienta (`buscarPrecio`) para obtener precios reales y calcular totales.
- Memoria conversacional por usuario (últimos 20 mensajes), aislada por ID de conversación.
- Chat web interactivo en una sola ventana (`index.html` servido por Spring Boot).
- Webhook de WhatsApp listo para 360dialog (sandbox gratis para pruebas).
- Proveedor de IA intercambiable: funciona con Groq (gratis), OpenAI y cualquier API compatible.

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 |
| IA | Spring AI 1.1 |
| Proveedor | Groq (llama-3.3-70b-versatile) / OpenAI |
| Frontend | HTML + CSS + JS vanilla (sin dependencias) |
| Build | Maven (con wrapper) |

## Estructura

```
src/main/java/demo/agent/
  QuoteAgentApplication.java     Punto de entrada
  QuoteAgent.java                Agente (system prompt + memoria + tools)
  PricingTools.java              Herramienta buscarPrecio (function calling)
  WhatsAppWebhookController.java Webhook de entrada de WhatsApp
  WhatsAppClient.java            Envío de respuestas por la API de 360dialog
  ChatDebugController.java       Endpoint de prueba /chat
src/main/resources/
  application.properties         Configuración (claves vía variables de entorno)
  static/index.html              Chat web
```

## Requisitos

- JDK 17 o superior
- Cuenta gratuita en [console.groq.com](https://console.groq.com) (API key `gsk_...`)
- (Opcional) Cuenta 360dialog para WhatsApp

## Configuración

Las claves se leen de variables de entorno (nunca están en el repositorio):

```powershell
$env:OPENAI_API_KEY = "gsk_tu_clave_groq"
$env:WHATSAPP_API_KEY = "tu_clave_360dialog"   # opcional para WhatsApp
```

Si usas OpenAI en lugar de Groq, cambia en `application.properties`:

```properties
spring.ai.openai.base-url=https://api.openai.com
spring.ai.openai.chat.options.model=gpt-4o-mini
```

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

El arranque tarda ~15-20 segundos. Al ver `Started QuoteAgentApplication`, abre:

```
http://localhost:8080
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/` | Chat web interactivo |
| GET | `/chat?cid=ID&msg=TEXTO` | Prueba directa del agente (con memoria por `cid`) |
| POST | `/webhook` | Recibe mensajes entrantes de WhatsApp (payload 360dialog) |
| GET | `/webhook` | Health check / verificación del webhook |

## WhatsApp (360dialog)

1. Obtén una API key gratis del sandbox enviando `START` al número de 360dialog (https://wa.me/4930609859535?text=START).
2. Expón tu servidor local con ngrok: `ngrok http 8080`.
3. En app.360dialog.io, configura el webhook con tu URL pública + `/webhook`.
4. Escribe al número sandbox desde tu teléfono y recibe las cotizaciones.

> El sandbox solo responde a tu propio número. Para producción se requiere verificación del negocio.

## Prueba rápida sin WhatsApp

```powershell
Invoke-RestMethod "http://localhost:8080/chat?msg=%C2%BFcu%C3%A1nto%20cuesta%20cambiar%204%20grifos%3F"
```

## Notas

- La memoria es en memoria: se pierde al reiniciar la app. Para persistirla, usa un almacén como Redis o PostgreSQL (starter `spring-ai-redis-store`).
- Los precios de ejemplo viven en `PricingTools.java`; cámbialos o conéctalos a una base de datos para el negocio real.
