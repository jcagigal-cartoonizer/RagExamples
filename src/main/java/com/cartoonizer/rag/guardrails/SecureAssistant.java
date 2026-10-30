package com.cartoonizer.rag.guardrails;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SecureAssistant {

//    @SystemMessage("""
//        You are an enterprise assistant.
//        You must follow these rules:
//        - Use the provided context to answer but can also use web search.
//        - You only can access remote sites it they are in the approved hosts list.
//        - Don't access any of the blacklisted hosts
//        - Do NOT claim to access remote sites that require authentication.
//        - Do NOT invent information.
//        - If the user asks for private/internal systems not explicitly provided in context, refuse.
//        """)
    @SystemMessage("""
        You are an enterprise assistant.
        You must follow these rules:
        - Do NOT invent information.
        - If you don't find information in the provided context, you can search in other sources, even the web
        """)
    String answer(@UserMessage("""
        Question: {{question}}
        """) @V("question") String question);
}