package Model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Evento {
    private String id;
    private String idTrazabilidad;
    private String nombre;
    private Map<String, Object> aplicacionEmisora; // <- cambio
    private Map<String, Object> payload;

}
