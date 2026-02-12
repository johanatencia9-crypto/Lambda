package utils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Servicio {

    private static Logger logger = LoggerFactory.getLogger(Servicio.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void llamarServicio(String urlServicio, String body, LambdaLogger logger, Map<String,Object> headers) {
        try {
            String[] headersArray = headers.entrySet().stream()
                    .flatMap(e -> Stream.of(e.getKey(), e.getValue().toString()))
                    .toArray(String[]::new);
            logger.log("[INFO] body " +body+ "\n");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlServicio))
                    .headers(headersArray)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();



            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Log en CloudWatch usando LambdaLogger
            logger.log("[INFO] Respuesta del servicio: " + response.statusCode() + " - " + response.body() + "\n");

        } catch (Exception e) {
            // Log de error en CloudWatch
            logger.log("[ERROR] Error al llamar el servicio: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    public static String obtenerToken(String urlServicio, LambdaLogger logger, Map<String,Object> headers,String body) {
        try {
            String[] headersArray = headers.entrySet().stream()
                    .flatMap(e -> Stream.of(e.getKey(), e.getValue().toString()))
                    .toArray(String[]::new);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlServicio))
                    .headers(headersArray)
                    .POST(HttpRequest.BodyPublishers.ofString(body))// no tiene body, se envíavacío porque el servicio de token no lo requiere
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.log("[INFO] Respuesta del servicio de token: " + response.statusCode() + " - " + response.body() + "\n");

            if (response.statusCode() == 200) {
                // Parsear JSON y extraer access_token
                Map<String, Object> json = MAPPER.readValue(response.body(), Map.class);
                String token = json.get("access_token") != null ? (String) json.get("access_token") : null;
                return token;
            } else {
                logger.log("[ERROR] Error al obtener token: Código de estado " + response.statusCode() + "\n");
                return null;
            }
        } catch (Exception e) {
            logger.log("[ERROR] Excepción al obtener token: " + e.getMessage() + "\n");
            e.printStackTrace();
            return null;
        }
    }

    public static String JsonBody(String contenido) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Limpiar caracteres de control invisibles
//        Map<String, Object> contenidoLimpio = limpiarStrings(contenido);

        // Mantener UTF-8 real, sin escapes
        mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Construir itemData
        Map<String, Object> itemData = new LinkedHashMap<>();
        itemData.put("Name", "ColaRetractosDigitalesPorvenir");
        itemData.put("Priority", "Normal");
        itemData.put("SpecificContent", contenido);
        itemData.put("Reference", "CC_123456789");
        itemData.put("Progress", "Salesforce");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("itemData", itemData);

        String jsonString ="";



        jsonString=String.format("""
            {
              "itemData": {
                "Name": "ColaRetractosDigitalesPorvenir",
                "Priority": "Normal",
                "SpecificContent":{
                  %s
                },
                "Reference": "CC_123456789",
                "Progress": "Salesforce"
              }
            }
            """,contenido);
        logger.info("JSON Body generado: " + jsonString);
        return jsonString;
    }


    private static Map<String, Object> limpiarStrings(Map<String, Object> contenido) {
        return contenido.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            if (entry.getValue() instanceof String str) {
                                // Elimina \r, \n, \t y espacios múltiples
                                return str.replaceAll("[\\r\\n\\t\\f]+", " ")
                                        .replaceAll("\\s{2,}", " ")
                                        .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]+", "")
                                        .trim();
                            }
                            return entry.getValue();
                        }
                ));
    }


}
