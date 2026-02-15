package helloworld;

import Model.InputQueen;
import Model.OutputResponse;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

class RPATranslatorLambda2Test {

    private RPATranslatorLambda2 handler;

    @Mock
    private LambdaLogger mockLogger;

    @BeforeEach
    void setUp() {
        handler = new RPATranslatorLambda2();
        mockLogger = mock(LambdaLogger.class);

        // Inicializar logger estático para tests
        RPATranslatorLambda2.logger = mockLogger;
    }

    @Test
    void processEvent_PayloadVacio_ProcesaEventoCompleto() throws Exception {
        // Input con payload vacío
        InputQueen input = new InputQueen();
        Map<String, Object> comando = new HashMap<>();
        comando.put("id", "123");
        comando.put("nombre", "eventoTest");
        comando.put("idTrazabilidad", "trace-001");
        comando.put("payload", null); // sin payload
        input.setComando(comando);

        OutputResponse output = new OutputResponse();

        // Mock estático de Servicio.obtenerToken para evitar llamadas externas
        try (var servicioMock = mockStatic(utils.Servicio.class)) {
            servicioMock.when(() ->
                    utils.Servicio.obtenerToken(anyString(), any(), anyMap(), anyString())
            ).thenReturn("fake-token");

            handler.processEvent(input, output);
        }

        assertEquals("Evento completo procesado.", output.getMensaje());
        assertNotNull(output.getPayload());
        verify(mockLogger, atLeastOnce()).log(contains("Evento recibido"));
        verify(mockLogger).log(contains("Token obtenido"));
    }

    @Test
    void processEvent_PayloadNoVacio_ProcesaPayload() throws Exception {
        InputQueen input = new InputQueen();
        Map<String, Object> payload = new HashMap<>();
        payload.put("key1", "value1");
        payload.put("key2", "value2");

        Map<String, Object> comando = new HashMap<>();
        comando.put("id", "456");
        comando.put("nombre", "eventoConPayload");
        comando.put("idTrazabilidad", "trace-002");
        comando.put("payload", payload);
        input.setComando(comando);

        OutputResponse output = new OutputResponse();

        try (var servicioMock = mockStatic(utils.Servicio.class)) {
            servicioMock.when(() ->
                    utils.Servicio.obtenerToken(anyString(), any(), anyMap(), anyString())
            ).thenReturn("fake-token");

            handler.processEvent(input, output);
        }

        assertEquals("Payload procesado correctamente.", output.getMensaje());
        assertNotNull(output.getPayload());
        assertEquals("value1", output.getPayload().get("key1"));
        verify(mockLogger, atLeastOnce()).log(contains("Evento recibido"));
        verify(mockLogger).log(contains("Token obtenido"));
    }


}
