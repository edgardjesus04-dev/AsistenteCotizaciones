# AGENTS.md — Contexto del proyecto

Handoff para retomar el desarrollo en cualquier sesión. Lee esto primero.

## Qué es
Agente de IA para cotizaciones de mantenimiento del hogar (demo/portfolio). Responde cotizaciones con Function Calling + memoria conversacional + chat web + webhook de WhatsApp. Con 13 años de experiencia en Java, es un proyecto de aprendizaje/portafolio en español.

## Stack y versiones (crítico: no cambiar sin verificar compatibilidad)
- Java 17 (máquina local tiene 17.0.12, NO 21)
- Spring Boot **3.5.16** (el Initializr genera Boot 4.1.0, que NO sirve: requiere Java 21 y Spring AI 2; quedamos en 3.5.x + Spring AI 1.1)
- Spring AI **1.1.8** (starter: `spring-ai-starter-model-openai`)
- Proveedor: **Groq** gratis (llama-3.3-70b-versatile), modelo `gpt-4o-mini` si se usa OpenAI
- Frontend: HTML/CSS/JS vanilla en `static/index.html`
- Build: Maven wrapper (`mvnw.cmd`), sin Maven instalado en el sistema

## Comandos
```powershell
# Compilar
.\mvnw.cmd -DskipTests package

# Ejecutar local (ESPERAR ~15-20s: no responde hasta "Started QuoteAgentApplication")
$env:OPENAI_API_KEY = "gsk_..."     # clave de Groq
.\mvnw.cmd spring-boot:run
# abrir http://localhost:8080
```

## Variables de entorno (nunca en archivos)
- `OPENAI_API_KEY` → clave de Groq (la app apunta a Groq vía base-url)
- `WHATSAPP_API_KEY` → clave 360dialog (OPCIONAL; si falta no debe romper el arranque)

## Configuración clave (`src/main/resources/application.properties`)
- `server.port=${PORT:8080}` → necesario para Render
- `spring.ai.openai.base-url=https://api.groq.com/openai` → SIN `/v1` al final: Spring AI agrega `/v1/chat/completions` solo
- `whatsapp.api-key=${WHATSAPP_API_KEY:}` → el `:` (default vacío) es obligatorio para que arranque sin la variable

## Trampas encontradas (no repetir)
1. `runtime: java` NO existe en Render (valores válidos: node, python, go, ruby, rust, elixir, docker, image, static). Java se despliega con `runtime: docker` + Dockerfile.
2. Spring AI 1.1.8 cambió la API de memoria: la clase es `InMemoryChatMemoryRepository` (no `InMemoryChatMemory`), y el bean `ChatMemory` lo auto-configura Spring (no crear manualmente). Clave de conversación: `ChatMemory.CONVERSATION_ID`. `MessageChatMemoryAdvisor.builder(chatMemory).build()` (sin `chatMemoryRetrievalSize`).
3. La tool `buscarPrecio` debe buscar por palabras clave con `contains`, NO coincidencia exacta (el modelo manda "cambio de grifo de cocina", no "plomeria").
4. Para probar el webhook con PowerShell: enviar el body como bytes UTF-8 (`[System.Text.Encoding]::UTF8.GetBytes($json)`) + `charset=utf-8`, si no da error JSON "Invalid UTF-8 start byte".
5. El arranque lento (~15s) es normal; el "se cuelga" del usuario fue siempre esto o placeholders sin resolver.

## Despliegue (Render, free)
- URL demo: https://asistente-cotizaciones.onrender.com
- Webhook WhatsApp: https://asistente-cotizaciones.onrender.com/webhook
- `render.yaml` (blueprint, runtime docker) + `Dockerfile` (multi-stage maven → temurin:17-jre)
- Auto-deploy en cada `git push` a main
- Plan gratis: duerme tras 15 min sin uso; primera petición ~30-60s

## Archivos principales
- `QuoteAgent.java` — agente (system prompt + tools + memoria)
- `PricingTools.java` — tool `buscarPrecio` (precios de ejemplo)
- `WhatsAppWebhookController.java` — POST `/webhook` (responde 200 y procesa async)
- `WhatsAppClient.java` — envía por API 360dialog (`https://waba.360dialog.io/v1/messages`, header `D360-API-KEY`)
- `ChatDebugController.java` — GET `/chat?cid=ID&msg=...` (prueba con memoria)
- `static/index.html` — chat web

## Git
- Repo: https://github.com/edgardjesus04-dev/AsistenteCotizaciones (rama `main`, credenciales vía Git Credential Manager)
- Flujo: `git add -A; git commit -m "..."; git push`

## Pendientes (para la próxima sesión)
1. Obtener clave 360dialog sandbox: enviar `START` a https://wa.me/4930609859535?text=START → luego configurarla en Render (Environment) y probar WhatsApp real.
2. (Opcional) Memoria persistente: Redis o Postgres (starter `spring-ai-redis-store`), la memoria actual es en memoria.
3. (Opcional) Seguridad: la clave de Groq quedó expuesta en un chat; recomendado revocarla y regenerar en https://console.groq.com/keys.
4. (Opcional) Conectar precios a una BD en vez de los valores de ejemplo.
