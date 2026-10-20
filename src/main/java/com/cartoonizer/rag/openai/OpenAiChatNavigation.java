package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.migration.prompters.PrompterViewToCompose;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import java.util.HashMap;
import java.util.Map;
import static java.util.stream.Collectors.joining;

public class OpenAiChatNavigation extends OpenAiChat {
    /*
    Don't process answerPath
    */
    public OpenAiChatNavigation(String apiKey, String modelName, String documentsPath, String answerPath, String processedAnswerPath) {
        super(apiKey, modelName, documentsPath, answerPath, processedAnswerPath);
    }
    @Override
    public String getPrompt(String question) {
        // Create a prompt for the model that includes question and relevant embeddings
        PrompterViewToCompose prompter = new PrompterViewToCompose(relevantEmbeddings, chatModel, documents, answerPath, processedAnswerPath, this.save);
        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability:\n"
                + "\n"
                + "Question:\n"
                + "{{question}}\n"
                + "\n");

        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().text())
                .collect(joining("\n\n"));
        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
//        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);
        AiMessage aiMessage = chatModel.chat(prompt.toUserMessage()).aiMessage();
        String answer = aiMessage.text();
        if (save) {
            prompter.saveAnswer(answerPath, answer);
        } else {
            System.out.println("ANSWER:");
            System.out.println(answer);
        }
        return answer;
    }
    
}
