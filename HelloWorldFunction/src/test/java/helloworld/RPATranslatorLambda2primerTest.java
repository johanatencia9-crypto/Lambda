package helloworld;

import Model.InputQueen;
import Model.OutputResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RPATranslatorLambda2primerTest {

    private RPATranslatorLambda2 handler;

    @Mock
    private Context mockContext;

    @Mock
    private LambdaLogger mockLogger;

    @BeforeEach
    void setUp() {
        handler = new RPATranslatorLambda2();
    }

    // -----------------------------
    // HANDLE REQUEST TESTS
    // -----------------------------

    @Test
    void handleRequest_BatchExitoso_ReturnSQSBatchResponseVacio() {

        SQSEvent.SQSMessage message = mock(SQSEvent.SQSMessage.class);
        when(message.getMessageId()).thenReturn("id-1");
        when(message.getBody()).thenReturn("{\"comando\":{\"id\":\"123\"}}");

        when(mockContext.getLogger()).thenReturn(mockLogger);

        SQSEvent event = new SQSEvent();
        event.setRecords(Collections.singletonList(message));

        RPATranslatorLambda2 spyHandler = spy(handler);
        doNothing().when(spyHandler).processEventSafe(any());

        SQSBatchResponse response = spyHandler.handleRequest(event, mockContext);

        assertTrue(response.getBatchItemFailures().isEmpty());
    }

    @Test
    void handleRequest_MensajeConBodyVacio_FallaIndividual() {

        SQSEvent.SQSMessage message = mock(SQSEvent.SQSMessage.class);
        when(message.getMessageId()).thenReturn("id-1");
        when(message.getBody()).thenReturn("");

        when(mockContext.getLogger()).thenReturn(mockLogger);

        SQSEvent event = new SQSEvent();
        event.setRecords(Collections.singletonList(message));

        SQSBatchResponse response = handler.handleRequest(event, mockContext);

        assertEquals(1, response.getBatchItemFailures().size());
        assertEquals("id-1",
                response.getBatchItemFailures().get(0).getItemIdentifier());
    }

    @Test
    void handleRequest_MultipleMensajes_UnaFalla() {

        SQSEvent.SQSMessage goodMessage = mock(SQSEvent.SQSMessage.class);
        when(goodMessage.getMessageId()).thenReturn("good-id");
        when(goodMessage.getBody()).thenReturn(
                "{\"comando\":{\"id\":\"123\",\"payload\":{},\"nombre\":\"test\",\"idTrazabilidad\":\"trace\"}}"
        );

        SQSEvent.SQSMessage badMessage = mock(SQSEvent.SQSMessage.class);
        when(badMessage.getMessageId()).thenReturn("bad-id");
        when(badMessage.getBody()).thenReturn("");

        when(mockContext.getLogger()).thenReturn(mockLogger);

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(goodMessage, badMessage));

        RPATranslatorLambda2 spyHandler = spy(handler);
        doNothing().when(spyHandler).processEventSafe(any());

        SQSBatchResponse response = spyHandler.handleRequest(event, mockContext);

        assertEquals(1, response.getBatchItemFailures().size());
        assertEquals("bad-id",
                response.getBatchItemFailures().get(0).getItemIdentifier());
    }

    // -----------------------------
    // PROCESS EVENT SAFE TESTS
    // -----------------------------

    @Test
    void processEventSafe_NoException_NoThrow() throws Exception {

        RPATranslatorLambda2 spyHandler = spy(new RPATranslatorLambda2());

        doNothing().when(spyHandler)
                .processEvent(any(), any());

        assertDoesNotThrow(() ->
                spyHandler.processEventSafe(mock(InputQueen.class))
        );
    }

    @Test
    void processEventSafe_Exception_LanzaRuntimeException() throws Exception {

        RPATranslatorLambda2 spyHandler = spy(new RPATranslatorLambda2());

        doThrow(new IllegalArgumentException("error"))
                .when(spyHandler)
                .processEvent(any(), any());

        assertThrows(RuntimeException.class, () ->
                spyHandler.processEventSafe(mock(InputQueen.class))
        );
    }

    @Test
    void processEvent_InputNull_LanzaIllegalArgument() throws Exception {

        RPATranslatorLambda2 handler = new RPATranslatorLambda2();

        assertThrows(IllegalArgumentException.class, () ->
                handler.processEvent(null, new OutputResponse())
        );
    }

    @Test
    void processEvent_ComandoNull_LanzaIllegalArgument() throws Exception {

        RPATranslatorLambda2 handler = new RPATranslatorLambda2();

        InputQueen input = new InputQueen();
        input.setComando(null);

        assertThrows(IllegalArgumentException.class, () ->
                handler.processEvent(input, new OutputResponse())
        );
    }






}
