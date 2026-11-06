
package com.cartoonizer.rag.shared;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DbAssistant {

    @SystemMessage(
            """
            You are an database admin. Answer the following question to the best of your ability using the provided tables local {{local}} and remote {{remote}}:\n"
                Question:
                {{question}}
            """
    )
    String answer(@UserMessage @V("local") String local, @V("remote") String remote, @V("question") String question);
}