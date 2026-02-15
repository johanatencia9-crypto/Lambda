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

class EventoTest {

    @Test
    void gettersYSetters_Basico() {
        Evento evento = new Evento();

        // Seteamos valores
        evento.setId("123");
        evento.setIdTrazabilidad("trace-001");
        evento.setNombre("EventoTest");

        Map<String, Object> appEmisora = new HashMap<>();
        appEmisora.put("nombreApp", "MiApp");
        evento.setAplicacionEmisora(appEmisora);

        Map<String, Object> payload = new HashMap<>();
        payload.put("campo1", "valor1");
        evento.setPayload(payload);

        // Verificamos getters
        assertEquals("123", evento.getId());
        assertEquals("trace-001", evento.getIdTrazabilidad());
        assertEquals("EventoTest", evento.getNombre());
        assertEquals(appEmisora, evento.getAplicacionEmisora());
        assertEquals(payload, evento.getPayload());
    }

    @Test
    void payloadYAplicacionEmisora_PuedenSerNulos() {
        Evento evento = new Evento();

        // No seteamos payload ni aplicacionEmisora
        assertNull(evento.getPayload());
        assertNull(evento.getAplicacionEmisora());
    }

    @Test
    void modificarPayloadYAplicacionEmisora_DebeActualizarCorrectamente() {
        Evento evento = new Evento();

        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("campo", "valor1");
        evento.setPayload(payload1);

        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("campo", "valor2");
        evento.setPayload(payload2);

        assertEquals("valor2", evento.getPayload().get("campo"));
    }
}
