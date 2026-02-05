package Model;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

@Getter
@Setter
public class OutputResponse {
    private String mensaje;
    private Map<String, Object> payload;


}
