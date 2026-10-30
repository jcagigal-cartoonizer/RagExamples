package com.cartoonizer.rag.openai;

import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.listener.AiServiceCompletedListener;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MyAiServiceCompletedListener implements AiServiceCompletedListener {
    @Override
    public void onEvent(AiServiceCompletedEvent event) {
//        InvocationContext invocationContext = event.invocationContext();
//        Optional<Object> res = event.result(); 
//        Object result = res.get();
////        System.out.println("MyAiServiceCompletedListener result = " + result + "\nCLASS = " + result.getClass().getName());
//        // The invocationId will be the same for all events related to the same LLM invocation
//        UUID invocationId = invocationContext.invocationId();
//        String aiServiceInterfaceName = invocationContext.interfaceName();
//        String aiServiceMethodName = invocationContext.methodName();
//        List<Object> aiServiceMethodArgs = invocationContext.methodArguments();
//        Object chatMemoryId = invocationContext.chatMemoryId();
//        Instant eventTimestamp = invocationContext.timestamp();
    }
}
