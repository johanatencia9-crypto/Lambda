package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Model.Evento;
import Model.InputQueen;
import Model.OutputResponse;
import lombok.extern.slf4j.Slf4j;
import utils.Servicio;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RPATranslatorLambda implements RequestHandler<Map<String, Object>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static LambdaLogger logger;

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        logger = context.getLogger();

        try {
            logger.log("Evento raw: " + MAPPER.writeValueAsString(input) + "\n");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {
            // ✅ PRIORIDAD 1: Extrae Records (case-insensitive)
            @SuppressWarnings("unchecked")
            Object recordsObj = input.get("Records") != null ? input.get("Records") : input.get("records");
            if (recordsObj instanceof List) {
                List<Map<String, Object>> records = (List<Map<String, Object>>) recordsObj;
                logger.log("✅ SQS Batch detectado: " + records.size() + " records\n");

                for (Map<String, Object> record : records) {
                    procesarRecord(record);
                }
                return "✅ Batch procesado: " + records.size();
            }

            // ✅ PRIORIDAD 2: Fallback directo (no SQS)
            logger.log("No Records → JSON directo\n");
            InputQueen directEvent = safeDeserialize(MAPPER.writeValueAsString(input), InputQueen.class);
            if (directEvent != null) {
                processEventSafe(directEvent);
                return "✅ Directo OK";
            }

            // ❌ Fallback final: trata como single body
            logger.log("Intentando como single body\n");
            InputQueen singleBody = safeDeserialize(MAPPER.writeValueAsString(input), InputQueen.class);
            processEventSafe(singleBody);

        } catch (Exception e) {
            logger.log("❌ CRÍTICO: " + e.getMessage() + "\n");
            throw new RuntimeException(e);
        }

        return "✅ OK";
    }

    private void procesarRecord(Map<String, Object> record) throws Exception {
        String body = (String) record.get("body");
        logger.log("Record body: " + body + "\n");

        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body SQS vacío");
        }

        InputQueen eventModel = MAPPER.readValue(body, InputQueen.class);
        processEventSafe(eventModel);
    }

    private void processEventSafe(InputQueen inputQueen) {
        OutputResponse output = new OutputResponse();
        try {
            processEvent(inputQueen, output);
            logger.log("✅ Evento procesado correctamente\n");
        } catch (Exception e) {
            logger.log("❌ Error procesando evento de negocio: " + e.getMessage() + "\n");
            throw new RuntimeException(e);
        }
    }

    private void processEvent(InputQueen inputQueen, OutputResponse output) throws Exception {
        if (inputQueen == null || inputQueen.getComando() == null) {
            logger.log("Error: el input o el comando es nulo.");
            output.setMensaje("Input o comando nulo");
            return;
        }

        Map<String, Object> comando = inputQueen.getComando();

        // Construimos el Evento a partir del comando
        Evento evento = new Evento();
        evento.setId((String) comando.get("id"));
        evento.setIdTrazabilidad((String) comando.get("idTrazabilidad"));
        evento.setNombre((String) comando.get("nombre"));

        Object aplicacion = comando.get("aplicacionEmisora");
        if (aplicacion instanceof Map) {
            evento.setAplicacionEmisora((Map<String, Object>) aplicacion);
        }

        Object payloadObj = comando.get("payload");
        if (payloadObj instanceof Map) {
            evento.setPayload((Map<String, Object>) payloadObj);
        }

        logger.log("Evento recibido: " + evento.getNombre() + ", ID: " + evento.getId() + "\n");

        JsonNode payload = MAPPER.valueToTree(evento.getPayload());

        if (payload != null && !payload.isNull() && payload.size() > 0) {
            MAPPER.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            Servicio servicio = new Servicio();
            logger.log("Payload recibido: " + payload.toString() + "\n");
            Map<String, Object> payloadMap = MAPPER.convertValue(payload, Map.class);
            output.setPayload(payloadMap);

            String jsonBody = "";
            int count = 0;
            int size = payloadMap.size();
            for (Map.Entry<String, Object> entry : payloadMap.entrySet()) {

                count++;

                jsonBody +=String.format( "\"" + entry.getKey() + "\"" +":" + "\""+entry.getValue().toString() +"\"");

                if (count < size) {
                    jsonBody += ",";
                }

                jsonBody +=System.lineSeparator();
            }

            jsonBody = servicio.JsonBody(jsonBody);
            // Servicio.llamarServicio(urlServicio, jsonBody, logger);
//           log.info( "JSON Body generado para servicio externo: " + jsonBody + "\n");
            output.setMensaje("Payload procesado correctamente.");
        } else {
            logger.log("Payload no encontrado, enviando evento completo.\n");
            JsonNode eventoCompleto = MAPPER.valueToTree(evento);
            String jsonBody = MAPPER.writeValueAsString(eventoCompleto);
            // Servicio.llamarServicio(urlServicio, jsonBody, logger);

            Map<String, Object> eventoCompletoMap = MAPPER.convertValue(eventoCompleto, Map.class);
            output.setPayload(eventoCompletoMap);

            output.setMensaje("Evento completo procesado.");
        }

        // Ejemplo de token (opcional según tu lógica)
        Map<String,Object> headersToken=new HashMap<>();
        headersToken.put("Content-Type","application/x-www-form-urlencoded");
        headersToken.put("scope","OR.Queues");

        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");

        String bodyToken = String.format(
                "grant_type=client_credentials&scope=%s&client_id=%s&client_secret=%s",
                URLEncoder.encode("OR.Queues", StandardCharsets.UTF_8),
                URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
        );

        String urlToken = System.getenv("URL_SERVICIO_TOKEN");
        String urlServicio = System.getenv("URL_SERVICIO_EXTERNO");

        String token = Servicio.obtenerToken(urlToken, logger, headersToken, bodyToken);


        logger.log("Token obtenido: " + token + "\n");

    }

    private <T> T safeDeserialize(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            logger.log("Deserialización falló [" + clazz.getSimpleName() + "]: " + e.getMessage() + "\nJSON: " + json);
            return null;
        }
    }
}

