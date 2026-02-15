package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Model.Evento;
import Model.InputQueen;
import Model.OutputResponse;
import lombok.extern.slf4j.Slf4j;
import utils.Servicio;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class RPATranslatorLambda2 implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    protected static LambdaLogger logger;

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {

        logger = context.getLogger();
        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();

        logger.log("Batch recibido con " + event.getRecords().size() + " mensajes\n");

        for (SQSEvent.SQSMessage message : event.getRecords()) {

            try {
                logger.log("Procesando messageId: " + message.getMessageId() + "\n");

                String body = message.getBody();

                if (body == null || body.isBlank()) {
                    throw new IllegalArgumentException("Body SQS vacío");
                }

                InputQueen eventModel = MAPPER.readValue(body, InputQueen.class);
                processEventSafe(eventModel);

                logger.log("✅ Mensaje procesado correctamente: " + message.getMessageId() + "\n");

            } catch (Exception e) {

                logger.log("❌ Error en mensaje " + message.getMessageId() + ": " + e.getMessage() + "\n");

                // Solo este mensaje se reintentará
                failures.add(
                        new SQSBatchResponse.BatchItemFailure(message.getMessageId())
                );
            }
        }

        return new SQSBatchResponse(failures);
    }

    protected  void processEventSafe(InputQueen inputQueen) {
        OutputResponse output = new OutputResponse();
        try {
            processEvent(inputQueen, output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void processEvent(InputQueen inputQueen, OutputResponse output) throws Exception {

        if (inputQueen == null || inputQueen.getComando() == null) {
            throw new IllegalArgumentException("Input o comando nulo");
        }

        Map<String, Object> comando = inputQueen.getComando();

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

            Map<String, Object> payloadMap = MAPPER.convertValue(payload, Map.class);
            output.setPayload(payloadMap);

            StringBuilder jsonBody = new StringBuilder();
            int count = 0;
            int size = payloadMap.size();

            for (Map.Entry<String, Object> entry : payloadMap.entrySet()) {
                count++;

                jsonBody.append("\"")
                        .append(entry.getKey())
                        .append("\":\"")
                        .append(entry.getValue())
                        .append("\"");

                if (count < size) {
                    jsonBody.append(",");
                }
                jsonBody.append(System.lineSeparator());
            }

            String jsonBodyStr = jsonBody.toString();
           String  body = servicio.JsonBody(jsonBodyStr.toString(),evento);

            output.setMensaje("Payload procesado correctamente.");


        } else {

            JsonNode eventoCompleto = MAPPER.valueToTree(evento);
            Map<String, Object> eventoCompletoMap =
                    MAPPER.convertValue(eventoCompleto, Map.class);

            output.setPayload(eventoCompletoMap);
            output.setMensaje("Evento completo procesado.");
        }

        // ==== TOKEN ====

        Map<String, Object> headersToken = new HashMap<>();
        headersToken.put("Content-Type", "application/x-www-form-urlencoded");
        headersToken.put("scope", "OR.Queues");

        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");



        String bodyToken = String.format(
                "grant_type=client_credentials&scope=%s&client_id=%s&client_secret=%s",
                URLEncoder.encode("OR.Queues", StandardCharsets.UTF_8),
                URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
        );

        String urlToken = System.getenv("URL_SERVICIO_TOKEN");

        String token = Servicio.obtenerToken(urlToken, logger, headersToken, bodyToken);

        logger.log("Token obtenido: " + token );
        RPAOrquestadorPost();
    }

    public void RPAOrquestadorPost () throws JsonProcessingException {
        String tableJson = System.getenv("TABLE_PARAMETER");

        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> tabla = mapper.readValue(tableJson, Map.class);

        for (Map.Entry<String, String> entry : tabla.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            logger.log("Clave: " + key + ", Valor: " + value );
        }









    }

}
