package Model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutputResponse {
    private String mensaje;
    private Map<String, Object> payload;


}
