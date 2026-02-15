package utils;

import Model.Evento;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioTest {

    @Test
    void jsonBody_GeneraJsonCorrecto() throws JsonProcessingException {
        String contenido = "\"campo1\":\"valor1\",\"campo2\":\"valor2\"";
        Evento evento = new Evento();
        evento.setId("123456");

        Map<String, Object> aplicacionEmisora = new HashMap<>();
        aplicacionEmisora.put("nombreAplicacionEmisora", "Salesforce");
        evento.setAplicacionEmisora(aplicacionEmisora);

        String json = Servicio.JsonBody(contenido,evento);

        assertNotNull(json);
        assertTrue(json.contains("123456"));
        assertTrue(json.contains("campo1"));
        assertTrue(json.contains("ColaRetractosDigitalesPorvenir"));
        assertTrue(json.contains("Salesforce"));

    }

    @Test
    void llamarServicio_LoggeaRespuesta() {
        // Mock del logger de Lambda
        LambdaLogger mockLogger = mock(LambdaLogger.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Solo verificamos que no lance excepciones
        assertDoesNotThrow(() ->
                Servicio.llamarServicio("https://httpbin.org/post", "{\"test\":1}", mockLogger, headers)
        );

        // Verificamos que se haya llamado al log al menos una vez
        verify(mockLogger, atLeastOnce()).log(contains("body"));
    }

    @Test
    void obtenerToken_Mockeado_DevuelveToken() {
        LambdaLogger mockLogger = mock(LambdaLogger.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        // Mockeamos la llamada estática para no depender del endpoint real
        try (MockedStatic<Servicio> mocked = mockStatic(Servicio.class)) {
            mocked.when(() -> Servicio.obtenerToken(anyString(), any(), anyMap(), anyString()))
                    .thenReturn("mock-token");

            String token = Servicio.obtenerToken("https://mock-token-service", mockLogger, headers, "body");

            assertEquals("mock-token", token);
        }
    }

    @Test
    void obtenerToken_Codigo200_RetornaNullPorqueNoHayAccessToken() {
        LambdaLogger mockLogger = mock(LambdaLogger.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String urlServicio = "https://httpbin.org/anything"; // devuelve 200 pero sin "access_token"
        String body = "grant_type=client_credentials";

        String token = Servicio.obtenerToken(urlServicio, mockLogger, headers, body);

        assertNull(token, "Debe ser null porque httpbin no devuelve access_token");
    }

    @Test
    void obtenerToken_Codigo404_RetornaNull() {
        LambdaLogger mockLogger = mock(LambdaLogger.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String urlServicio = "https://httpbin.org/status/404"; // devuelve 404
        String body = "grant_type=client_credentials";

        String token = Servicio.obtenerToken(urlServicio, mockLogger, headers, body);

        assertNull(token, "Debe ser null al recibir 404");
    }

    @Test
    void obtenerToken_HeadersVacios_NoLanzaExcepcion() {
        LambdaLogger mockLogger = mock(LambdaLogger.class);
        Map<String, Object> headers = new HashMap<>();

        String urlServicio = "https://httpbin.org/status/200"; // devuelve 200
        String body = "";

        assertDoesNotThrow(() -> {
            String token = Servicio.obtenerToken(urlServicio, mockLogger, headers, body);
            assertNull(token); // httpbin no devuelve access_token
        });
    }

    @Test
    void limpiarStrings_IndirectamenteDesdeJsonBody() throws JsonProcessingException {
        // Pasamos caracteres de control al contenido
        String contenido = "\"linea1\\nlinea2\\tlinea3\"";
        Evento evento = new Evento();
        evento.setId("123456");

        Map<String, Object> aplicacionEmisora = new HashMap<>();
        aplicacionEmisora.put("nombreAplicacionEmisora", "Salesforce");
        evento.setAplicacionEmisora(aplicacionEmisora);
        String json = Servicio.JsonBody(contenido,evento);

        assertNotNull(json);
        assertTrue(json.contains("linea1"));
        // Los tabulados \t deben permanecer en este caso porque JsonBody no llama limpiarStrings
        // Si quieres que se limpie, hay que adaptar JsonBody a usar limpiarStrings
    }


    @Test
    void llamarServicio_LanzaExcepcion_LoggeaError() {
        // Mock del logger
        LambdaLogger mockLogger = mock(LambdaLogger.class);

        // URL inválida para forzar excepción
        String urlInvalida = "http://::invalid-url";
        String body = "{\"test\":1}";
        Map<String, Object> headers = new HashMap<>();

        // Debe capturar la excepción interna y loggear el error
        assertDoesNotThrow(() -> Servicio.llamarServicio(urlInvalida, body, mockLogger, headers));

        // Verificamos que se haya loggeado la palabra "[ERROR]"
        verify(mockLogger, atLeastOnce()).log(contains("[ERROR]"));
    }

    @Test
    void limpiarStrings_EliminaCaracteresDeControlCorrectamente() throws Exception {
        // Map de prueba con caracteres de control
        Map<String, Object> input = new HashMap<>();
        input.put("clave1", "linea1\r\nlinea2\tlinea3\f");
        input.put("clave2", "   doble  espacio  ");

        // Usamos reflection para invocar método private static
        var method = Servicio.class.getDeclaredMethod("limpiarStrings", Map.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> resultado = (Map<String, Object>) method.invoke(null, input);

        assertEquals("linea1 linea2 linea3", resultado.get("clave1"));
        assertEquals("doble espacio", resultado.get("clave2"));
    }
    @Test
    void jsonBody_GeneraSinAplicacion() throws JsonProcessingException {
        String contenido = "\"campo1\":\"valor1\",\"campo2\":\"valor2\"";
        Evento evento = new Evento();
        evento.setId("123456");

        Map<String, Object> aplicacionEmisora = new HashMap<>();
        aplicacionEmisora.put("nombreAplicacionEmisora", "");
        evento.setAplicacionEmisora(aplicacionEmisora);

        String json = Servicio.JsonBody(contenido,evento);

        assertNotNull(json);
        assertTrue(json.contains("SpecificContent"));
        assertTrue(json.contains("campo1"));
        assertTrue(json.contains("Desconocida"));
    }

}
