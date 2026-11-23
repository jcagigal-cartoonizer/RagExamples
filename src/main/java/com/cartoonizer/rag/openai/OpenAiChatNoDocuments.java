package com.cartoonizer.rag.openai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import com.cartoonizer.rag.shared.Assistant;
import com.cartoonizer.rag.utils.ApiKeys;
import static com.cartoonizer.rag.utils.ApiKeys.CLAUDE_API_KEY;
import static com.cartoonizer.rag.utils.ApiKeys.OPENAI_API_KEY;
import com.cartoonizer.rag.utils.ModelNames;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatRequestParameters;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class OpenAiChatNoDocuments implements AiChatInterface {

    public static void main(String[] args) {
        // For migration to compose this costs 10 times more OpenAi credits
//        OpenAiChatNoDocuments chat = new OpenAiChatNoDocuments(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI);
        OpenAiChatNoDocuments chat = new OpenAiChatNoDocuments(ApiKeys.CLAUDE_API_KEY, ModelNames.CLAUDE_HAIKU);
        String answer = chat.askQuestion("Please, give an example of how to write an ACP-compatible AI Agent using langchain4j and Claude or OpenAI");
//        System.out.println("ANSWER = " + answer);
    }
    private String theApiKey;
    private String theModelName;
    public Assistant assistant;

    public OpenAiChatNoDocuments(String apiKey, String modelName) {
        this.theApiKey = apiKey;
        this.theModelName = modelName;
        assistant = createAssistant();
        System.out.println("*** OpenAiChatNoDocuments theModelName " + theModelName); //  + " apiKey " + apiKey);
    }

    @Override
    public String askQuestion(String question) {
        String answer = "";
        if (CLAUDE_API_KEY.equals(theApiKey)){
//            answer = this.chatModel.chat(question);
            ChatRequestParameters params = this.chatModel.defaultRequestParameters();
            ChatRequest chatRequest = ChatRequest.builder()
                .parameters(params)
                .messages(
                    SystemMessage.from("You are an expert in creating AI agents using Claude or OpenAi. Answer the question to the best of your ability"),
                    UserMessage.from(question)
            )
            .build();
            ChatResponse response = this.chatModel.doChat(chatRequest);            
            answer = response.aiMessage().text();
        } else {
            answer = this.assistant.answer(question);
        }
        System.out.println("ANSWER = " + answer);
        return answer;
    }

    public void askQuestions() {
        String[] questions = this.getQuestions();
        String[] answers = new String[questions.length];
        StringBuilder theAnswer = new StringBuilder();
        for (int i = 0; i < questions.length; i++) {
//                System.out.println("\n\n****************************************************************************************");
            String question = questions[i];
//                System.out.println("QUESTION: " + question);
//            answers[i] = askQuestion(question);
            theAnswer.append("\n// ANSWER\n");
            theAnswer.append(answers[i]);
            System.out.println("ANSWER: " + answers[i]);
            System.out.println("\n****************************************************************************************");
        }
    }

    protected String[] getQuestions() {
        String[] questions = {
            """
            I have this description of a project that uses LangChain4j
            Can you give a more technical and more senior/impact-focused version for a portfolio?
            TITLE
AI powered online shop search Agent using RAG
            THE TASK
            Develop an AI assisted search component for an online shop that should let the visitors search for products the same way they would do it with ChatGPT.
            Use OpenAI for this, but ensure that the answers come only from the online shop catalog, never from other competing shops.
            The users will write their search in natural language and the AI Agent will reply with a list of the products that meet the customer prompt.
            The customer will be also able to follow up a conversation with the agent asking it to refine its previous answer or ask the agent more questions.
            An importing requirement is to keep the token cost at a minimum.
            
            WHAT I DID
            I developped a RAG system using an OpenAI model and the LangChain4j OpenAI integration. 
            
            To fullfill the requirement that the Agent must use only information from the online shop, I wrote and AI Assistant that does the following:
            When the user starts a chat session (opens the search/chat window):
            - Fetch some documents with relevant information from an internal folder.
            - Start an Agent that fetchs the shop's catalog in JSON format every 12 hours (the catalog is updated at most once a day). 
            - Use the standard LangChain4j DocumentByLineSplitter to create chunks of text from the loaded files
            - Write a custom DocumentSplitter to parse the JSON catalog, add some metadata to each product and create a text segment for every line.
            - Instantiate an EmbeddingModel and EmbeddingStore
            - Instantiate an EmbeddingStoreIngestor using the splitter, the embedding model and the embedding store
            - Ingest the embedded documents
            - Instantiate an OpenAI chat model
            - Instantiate a QueryTransformer
            - Instantiate a ContentRetriever using the embedding model and the embedding store
            - Instantiate a RetrievalAugmentor with the transformer and the retriever
            - Instantiate the Assistant using the chat model, the retrieval augmentor and a chat memory window of 20 messages.
            
            When the user posts a question:
            - Use the Assistant to fetch the answer to the question with OpenAI.
            
            When the assistant receives the answer to the question:
            - Process the answer to extract product information and present the user with a list of products with the product details and link to add it to the shopping cart.
            
            Regarding the requirement to keep the token cost at a minimum, it's achieved as a side effect, because OpenAI only needs to use a small set of data.
            The assistant keeps count of the tokens used and checks if they are greater than the configured monthly maximum.
            
            SUMMARY
            The result is like a "Private ChatGPT" where the visitors can ask questions like if they were using ChatGPT, but we send the question to OpenAI requiring it to first use our catalog and other internal information and we process the answer to extract product information and present the user with a list of products with the product details and link to add it to the shopping cart.
            
                        
            """
        };
        return questions;
    }
    public ChatModel chatModel;
    private Assistant createAssistant() {
        if (OPENAI_API_KEY.equals(theApiKey)) {
            chatModel = OpenAiChatModel.builder()
                .apiKey(theApiKey)
                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                .modelName(theModelName)
                .build();
        } else if (CLAUDE_API_KEY.equals(theApiKey)){
            chatModel = AnthropicChatModel.builder()
                .apiKey(theApiKey)
//                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                .modelName(theModelName)
                .maxTokens(64000)
                .build();
        } else {
            chatModel = OllamaChatModel.builder()
                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                .modelName(theModelName)
                .build();
        }

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    @Override
    public void init() {
    }

}
