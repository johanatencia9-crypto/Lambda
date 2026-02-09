package helloworld;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class TestContext implements Context {

    @Override
    public String getAwsRequestId() {
        return "test-request-id";
    }

    @Override
    public String getLogGroupName() {
        return "/aws/lambda/test-function";
    }

    @Override
    public String getLogStreamName() {
        return "2026/02/08/[$LATEST]abc123";
    }

    @Override
    public String getFunctionName() {
        return "RPATranslatorLambda";
    }

    @Override
    public String getFunctionVersion() {
        return "$LATEST";
    }

    @Override
    public String getInvokedFunctionArn() {
        return "arn:aws:lambda:us-east-1:123456789:function:RPATranslatorLambda";
    }

    @Override
    public CognitoIdentity getIdentity() {
        return null;
    }

    @Override
    public ClientContext getClientContext() {
        return null;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return 300000; // 5 minutos
    }

    @Override
    public int getMemoryLimitInMB() {
        return 512;
    }

    @Override
    public LambdaLogger getLogger() {
        return new LambdaLogger() {
            @Override
            public void log(String message) {
                System.out.println("🔍 TEST LOG: " + message);
            }

            @Override
            public void log(byte[] message) {
                log(new String(message));
            }
        };
    }
}
