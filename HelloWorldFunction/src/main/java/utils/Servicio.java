package utils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Servicio {

    public static void llamarServicio(String urlServicio, String body, LambdaLogger logger) {
        try {
            logger.log("[INFO] body " +body+ "\n");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlServicio))
                    .header("Content-Type", "application/json")
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
}
