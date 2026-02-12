package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utils.Servicio;
import Model.Evento;
import Model.InputQueen;
import Model.OutputResponse;


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
        try {
            processEvent(inputQueen);
            logger.log("Evento procesado correctamente\n");
        } catch (Exception e) {
            logger.log("❌ Error procesando evento de negocio: " + e.getMessage() + "\n");
            // Si quieres retry de SQS, relanza
            throw new RuntimeException(e);
        }
    }

    private void processEvent(InputQueen inputQueen) throws Exception {

        if (inputQueen == null || inputQueen.getEvento() == null) {
            throw new IllegalArgumentException("Input o evento nulo");
        }

        Evento evento = inputQueen.getEvento();
        logger.log("Evento recibido: " + evento.getNombre() + ", ID: " + evento.getId() + "\n");

        OutputResponse output = new OutputResponse();
        Object payload = evento.getPayload();
        String jsonBody;

        if (payload != null) {
            Map<String, Object> mapPayload = (Map<String, Object>) payload;

            jsonBody = MAPPER.writeValueAsString(payload);

            if (jsonBody.equals("{}")) {
                logger.log("Payload detectado como vacío, enviando evento completo");
                jsonBody = MAPPER.writeValueAsString(evento);
            } else {
                StringBuilder sb = new StringBuilder();

                int size = mapPayload.size();
                int count = 0;

                for (Map.Entry<String, Object> entry : mapPayload.entrySet()) {

                    count++;

                    sb.append("\"")
                            .append(entry.getKey())
                            .append("\":\"")
                            .append(entry.getValue().toString())
                            .append("\"");

                    if (count < size) {
                        sb.append(",");
                    }

                    sb.append(System.lineSeparator());
                }


                jsonBody = sb.toString();




            }

            // Llamada a servicio

        } else {
            logger.log("Payload es null, enviando evento completo\n");
            jsonBody = MAPPER.writeValueAsString(evento);



        }
        Map<String,Object> headersToken=new HashMap<>();
        headersToken.put("Content-Type","application/x-www-form-urlencoded");
        headersToken.put("Cookie","_cfuvid=VCYp4F4EUTofToNU8vju9vf_N_U0SA6Jm1fN8xXx5Wk-1770850471950-0.0.1.1-604800000");

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
        String Tokent= Servicio.obtenerToken(urlToken, logger, headersToken, bodyToken);
//        Servicio.llamarServicio(urlServicio, jsonBody, logger,);
//        output.setPayload((Map<String, Object>) payload);
//
        output.setMensaje("Evento procesado correctamente");
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
