package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.utils.ApiKeys;
import com.cartoonizer.rag.utils.ModelNames;
import java.io.File;

public class SimpleQuestionsOpenAi {
    public static final String PROFILE = 
                            """
                I'm a software developer specializing in AI development and I'm looking for freelance jobs 
                I have this presentation: 
                I’m a software developer since 1990 (freelance since 1993), working for companies (from small to large), public entities (ministries of Health and Defense of Spain, several regional governments) and banks, directly or thru consulting companies.
                
                I’ve worked with many languages (C/C++, Java, Kotlin, PHP, JavaScript, PHP, Perl … even Cobol in my first 2+ years), domains/areas (desktop apps, web frontend and backend, smartphone apps, image processing apps and plugins for 3D software, ...) and Oss (linux, windows, macOS/iOS).
                
                I’m currently specializing in AI assisted development (RAG, AI agents, multi-agents orchestration, ML pipeline …).
                
                In the last 2+ months I’ve developed a set of tools to migrate a JetPack Views Android app to JetPack Compose, using LangChain4j with OpenAi models. With these tools, the time to migrate the app can be reduced 4 or 5 fold.
                
                But this is only one of the possible uses of RAG. There are many other ways to apply AI to your company using RAG as a mean to customize and fine tune your AI pipeline using your own data.
                
                Tell me your needs and we can arrange a meeting to discuss it, so I can provide you with an initial estimate. 
                
                What I can offer:
                - Integration of RAG processes with your software, with or without AI Agents.
                - Development of AI Agents customized for your needs.
                - Coordination of internal and external AI agents,  using the A2A protocol.
                -  Development of Agent Tools (for retrieval and processing of your files, databases, …) to be used by other AI Agents. 
                - Combining these RAG and Agentic tools to improve your Application development pipeline.
                - Consulting for your AI projects
                
                I use  LangChain4j because is more flexible than  LangChain Python and let me leverage the full ecosystem of Java libs and the tools that I’ve developed in my 25+ years of experience with Java (to create AI Tools and RAG pipelines, for instance). 
                                
                Please provide a freelance platform profile based on this profile taking into account what clients of the Malt platform are asking for.
                """;
    
    public static final String OTHER = 
                """
                I want to process some kotlin source files to create a list of all classes, composables and variables.
                How can I do that?
                """;
    
    public static void main(String[] args) {
        String documentsPath = "./example-files";
        File f = new File(documentsPath);
        String[] questions = {
//            PROFILE,
            OTHER,
        };
        if (f.exists() && questions.length > 0) {
            System.out.println("Before setup ");
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, null, null);
            chat.setup();
            String[] answers = new String[questions.length];
            for (int i = 0; i < questions.length; i++) {
                System.out.println("\n\n****************************************************************************************");
                String question = questions[i];
                if (!question.isEmpty()) {
                     System.out.println("QUESTION: " + question);
                    answers[i] = chat.askQuestion(question, 5, 0.7, false); // don't save answer
                    System.out.println("ANSWER: " + answers[i]);
                }
            }
            System.out.println("\n\n****************************************************************************************");
        }
    }

}
