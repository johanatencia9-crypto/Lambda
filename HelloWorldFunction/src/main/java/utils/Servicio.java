package utils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.stream.Stream;

public class Servicio {

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
                    .POST(HttpRequest.BodyPublishers.ofString(body))
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

    public static String bodyToken() throws JsonProcessingException {
        Map <String, String> bodyMap = Map.of(
                "client_id", System.getenv("CLIENT_ID"),
                "client_secret", System.getenv("CLIENT_SECRET"),
                "grant_type", "client_credentials"
        );

        String body=MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(bodyMap);
        return body;
    }
}
