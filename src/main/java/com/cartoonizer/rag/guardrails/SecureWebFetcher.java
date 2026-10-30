package com.cartoonizer.rag.guardrails;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SecureWebFetcher {

    private final SecurityPolicy securityPolicy;
    private final HttpClient httpClient;

    public SecureWebFetcher(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public String fetch(URI uri) throws IOException, InterruptedException {
        if (!securityPolicy.isAllowedRemoteUri(uri)) {
            throw new SecurityException("Blocked remote access to: " + uri);
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            throw new SecurityException("Blocked or failed remote fetch, status=" + response.statusCode());
        }
        String body = response.body();
        System.out.println("=== SecureWebFetcher uri " + uri.toString() + "\n" + body);
        return body;
    }
}
