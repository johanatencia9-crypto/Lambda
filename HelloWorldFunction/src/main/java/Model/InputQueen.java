
package Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)

public class InputQueen {
    private String id;
    private String idTrazabilidad;
    private String nombre;
    private Evento evento;
}
