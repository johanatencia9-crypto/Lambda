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

class InputQueenTest {

    @Test
    void getterYSetter_Comando() {
        InputQueen input = new InputQueen();

        // Seteamos un mapa
        Map<String, Object> comando = new HashMap<>();
        comando.put("id", "123");
        comando.put("nombre", "test");

        input.setComando(comando);

        // Verificamos getter
        assertEquals(comando, input.getComando());
        assertEquals("123", input.getComando().get("id"));
        assertEquals("test", input.getComando().get("nombre"));
    }

    @Test
    void comando_PuedeSerNulo() {
        InputQueen input = new InputQueen();

        // Inicialmente null
        assertNull(input.getComando());

        // Seteamos null explícitamente
        input.setComando(null);
        assertNull(input.getComando());
    }

    @Test
    void comando_PuedeSerMapaVacio() {
        InputQueen input = new InputQueen();
        Map<String, Object> comandoVacio = new HashMap<>();

        input.setComando(comandoVacio);

        assertNotNull(input.getComando());
        assertTrue(input.getComando().isEmpty());
    }

    @Test
    void comando_ModificarMapa_ReflejaCambios() {
        InputQueen input = new InputQueen();
        Map<String, Object> comando = new HashMap<>();
        comando.put("campo1", "valor1");
        input.setComando(comando);

        // Modificamos el mapa
        input.getComando().put("campo2", "valor2");

        assertEquals(2, input.getComando().size());
        assertEquals("valor2", input.getComando().get("campo2"));
    }
}
