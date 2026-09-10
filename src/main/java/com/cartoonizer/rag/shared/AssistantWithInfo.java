
package com.cartoonizer.rag.shared;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AssistantWithInfo {

    @SystemMessage(
            "Answer the following question to the best of your ability:\n"
                + "\n"
                + "Question:\n"
                + "{{question}}\n"
                + "\n"
                + "Base your answer on the following information:\n"
                + "{{information}}")
        String answerWithInfo(@UserMessage String question, @UserMessage String information);
}