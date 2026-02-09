package helloworld;

import Model.Evento;
import Model.InputQueen;
import Model.OutputResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utils.Servicio;

import java.util.Map;


public class RPA implements RequestHandler<InputQueen, OutputResponse> {


    @Override
    public OutputResponse handleRequest(InputQueen inputQueen, Context context) {
        ObjectMapper mapper = new ObjectMapper();
        String URL = System.getenv("URL_SERVICIO_EXTERNO");
        LambdaLogger logger = context.getLogger();
        OutputResponse output = new OutputResponse();


        if (inputQueen == null || inputQueen.getEvento() == null) {
            logger.log("Error: el input o el evento es nulo.");
            return output;
        }

        try {
            Evento evento = inputQueen.getEvento();
            logger.log("Evento recibido: " + evento.getNombre() + ", ID: " + evento.getId() + "\n");

            JsonNode payload = mapper.valueToTree(evento.getPayload());

            if (payload != null && !payload.isNull()) {
                logger.log("Payload recibido: " + payload.toString() + "\n");
                Map<String, Object> payloadMap = mapper.convertValue(payload, Map.class);
                output.setPayload(payloadMap);
                String jsonBody = mapper.writeValueAsString(payload);
                Servicio.llamarServicio(URL, jsonBody, context.getLogger());

                output.setMensaje("Payload procesado correctamente.");
            } else {
                logger.log("Payload no encontrado, enviando evento completo.\n");

                // Convertimos el evento completo a JsonNode

                JsonNode eventoCompleto = mapper.valueToTree(evento);
                String jsonBody = mapper.writeValueAsString(eventoCompleto);
                Servicio.llamarServicio(URL, jsonBody, context.getLogger());
                Map<String, Object> eventoCompletoMap = mapper.convertValue(eventoCompleto, Map.class);
                output.setPayload(eventoCompletoMap);
                output.setMensaje("Evento completo procesado.");
            }

        } catch (Exception e) {
            logger.log("Excepción al procesar el evento: " + e.getMessage() + "\n");
            output.setMensaje("Excepción: " + e.getMessage());
        }

        return output;
    }

}
