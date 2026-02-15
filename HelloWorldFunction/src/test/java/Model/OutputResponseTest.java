package Model;

import Model.Evento;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutputResponseTest {

    @Test
    void getterYSetter_Mensaje() {
        OutputResponse output = new OutputResponse();
        output.setMensaje("Mensaje de prueba");

        assertEquals("Mensaje de prueba", output.getMensaje());
    }

    @Test
    void getterYSetter_Payload() {
        OutputResponse output = new OutputResponse();
        Map<String, Object> payload = new HashMap<>();
        payload.put("campo1", "valor1");

        output.setPayload(payload);

        assertEquals(payload, output.getPayload());
        assertEquals("valor1", output.getPayload().get("campo1"));
    }

    @Test
    void payload_PuedeSerNulo() {
        OutputResponse output = new OutputResponse();
        output.setPayload(null);

        assertNull(output.getPayload());
    }

    @Test
    void payload_PuedeSerMapaVacio() {
        OutputResponse output = new OutputResponse();
        Map<String, Object> payloadVacio = new HashMap<>();
        output.setPayload(payloadVacio);

        assertNotNull(output.getPayload());
        assertTrue(output.getPayload().isEmpty());
    }

    @Test
    void modificarMapa_ReflejaCambios() {
        OutputResponse output = new OutputResponse();
        Map<String, Object> payload = new HashMap<>();
        payload.put("campo1", "valor1");

        output.setPayload(payload);

        // Modificar el mapa después de setear
        output.getPayload().put("campo2", "valor2");

        assertEquals(2, output.getPayload().size());
        assertEquals("valor2", output.getPayload().get("campo2"));
    }

    @Test
    void mensaje_PuedeSerNulo() {
        OutputResponse output = new OutputResponse();
        output.setMensaje(null);

        assertNull(output.getMensaje());
    }
}
