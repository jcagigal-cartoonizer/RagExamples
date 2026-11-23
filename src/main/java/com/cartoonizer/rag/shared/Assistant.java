
package com.cartoonizer.rag.shared;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Assistant {

    @SystemMessage(
            """
//            You are in charge of an online shop. Answer the following question to the best of your ability using the shop data:\n"
            You are an expert in creating AI agents using Claude or OpenAi and langchain4j. Answer the following question to the best of your ability:\n"
            Question:
                {{question}}
            """
    )
    String answer(@UserMessage @V("question") String question);
}