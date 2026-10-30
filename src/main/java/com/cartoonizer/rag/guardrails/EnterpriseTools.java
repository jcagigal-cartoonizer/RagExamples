/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cartoonizer.rag.guardrails;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnterpriseTools {

    private final SecurityPolicy policy;
    private final ApprovedResourceRetriever retriever;
    private final SecureWebFetcher webFetcher;

    public EnterpriseTools(SecurityPolicy policy,
                           ApprovedResourceRetriever retriever,
                           SecureWebFetcher webFetcher) {
        this.policy = policy;
        this.retriever = retriever;
        this.webFetcher = webFetcher;
    }

    @Tool("Search approved enterprise knowledge only. This searches local approved documents and returns relevant excerpts.")
    public String searchEnterpriseKnowledge(String query) {
        List<TextSegment> segments = retriever.retrieve(query);
        System.out.println("*** EnterpriseTools searchEnterpriseKnowledge retrieve query = " + query + " Segments size = " + retriever.getTextSegments().size());

        if (segments.isEmpty()) {
            return "EnterpriseTools searchEnterpriseKnowledge NO_APPROVED_CONTEXT_FOUND";
        }
        String segmentsString = segments.toString();
        System.out.println("===================== *** EnterpriseTools searchEnterpriseKnowledge");
        System.out.println(segmentsString);
        System.out.println("==========================================");
        return segmentsString;
//        Stream<TextSegment> theSegments = segments.stream();
//        return theSegments.map(seg -> {
//                    try {
//                        Metadata meta = seg.metadata();
//                        String source = meta.getString("source");
//                        return "[SOURCE: " + source + "]\n" + seg.text();
//                    } catch (Exception e) {
//                        System.out.println("*** EnterpriseTools searchEnterpriseKnowledge ERROR " + e);
//                        return "";
//                    }
//                })
//                .collect(Collectors.joining("\n---\n"));
    }

    @Tool("Fetch a document only from an approved enterprise remote host. No authenticated or private sites allowed.")
    public String getApprovedRemoteDocument(String url) {
        try {
            URI uri = URI.create(url);

            if (!policy.isAllowedRemoteUri(uri)) {
                return "BLOCKED: remote URL is not allowlisted";
            }

            return webFetcher.fetch(uri);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Tool("List the approved local folders that may be accessed.")
    public String listApprovedFolders() {
        return policy.allowedFolders().stream()
                .map(path -> path.toAbsolutePath().normalize().toString())
                .collect(Collectors.joining("\n"));
    }
    
}
