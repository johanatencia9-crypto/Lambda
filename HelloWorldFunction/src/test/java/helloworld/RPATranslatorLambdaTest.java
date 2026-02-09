package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import helloworld.TestContext;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RPATranslatorLambdaTest {
    private final RPATranslatorLambda lambda = new RPATranslatorLambda();
    private final Context context = new TestContext(); // Tu TestContext


    @Test
    public void handleRequest() throws JsonProcessingException {

        String event="{\n" +
                "  \"id\": \"a929552a-0910-4fbe-ab2a-589170aeea49\",\n" +
                "  \"idTrazabilidad\": \"Número caso Salesforce\",\n" +
                "  \"nombre\": \"RPA\",\n" +
                "  \"evento\": {\n" +
                "    \"id\": \"evt-001\",\n" +
                "    \"idTrazabilidad\": \"trace-001\",\n" +
                "    \"nombre\": \"EventoRPA\",\n" +
                "    \"aplicacionEmisora\": {\n" +
                "      \"idAplicacionEmisora\": \"RPA\",\n" +
                "      \"nombreAplicacionEmisora\": \"RPACIN\",\n" +
                "      \"idTransaccionEmisora\": \"xx\",\n" +
                "      \"fechaTransaccion\": \"2017-07-21T13:05:00\"\n" +
                "    },\n" +
                "    \"payload\": {\n" +
                "      \"process\": \"Historia-Laboral\",\n" +
                "      \"idUsuario\": \"RPA\",\n" +
                "      \"TipoIdCliente\": \"CC\",\n" +
                "      \"NumeroIdCliente\": \"123456\",\n" +
                "      \"idSesion\": \"abc-123\",\n" +
                "      \"idSolicitud\": \"SF-001\",\n" +
                "      \"tipo\": \"tema sin apellido\",\n" +
                "      \"subTipo\": \"detalle sin apellido\",\n" +
                "      \"adjuntos\": [\n" +
                "        {\n" +
                "          \"id\": \"adjunto6_page_02\",\n" +
                "          \"objectId\": \"obj-001\",\n" +
                "          \"nombreArchivo\": \"adjunto6_page_02.pdf\",\n" +
                "          \"numeroPagina\": 1\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}\n";


        Map<String, Object> eventMap =
                new ObjectMapper().readValue(event, Map.class);

        String resultado = lambda.handleRequest(eventMap,  context);
       assertEquals("✅ Directo OK", resultado);
        assertNotNull(resultado);

    }
}