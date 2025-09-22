package com.example.webhooksolver.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

@Service
public class WebhookService {

    private final WebClient webClient;

    @Value("${app.name}")
    private String name;
    @Value("${app.regNo}")
    private String regNo;
    @Value("${app.email}")
    private String email;

    @Value("${app.generate-webhook-url}")
    private String generateWebhookUrl;

    @Value("${app.test-webhook-url}")
    private String testWebhookUrl;

    public WebhookService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public record GenerateResp(@JsonProperty("webhook") String webhook,
                               @JsonProperty("accessToken") String accessToken) {
        public String getWebhookUrl() { return webhook; }
        public String getAccessToken() { return accessToken; }
    }

    public GenerateResp generateWebhook() {
        Map<String, String> body = Map.of(
                "name", name,
                "regNo", regNo,
                "email", email
        );

        GenerateResp resp = webClient.post()
                .uri(generateWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GenerateResp.class)
                .block(); // block since this is startup synchronous flow

        if (resp == null || resp.getWebhookUrl() == null || resp.getAccessToken() == null) {
            throw new RuntimeException("Invalid response from generateWebhook: " + resp);
        }
        return resp;
    }

    public String getRegNo() {
        return regNo;
    }

    public int getLastTwoDigits(String regNo) {
        // extract digits from end
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.length() >= 2) {
            String lastTwo = digits.substring(digits.length() - 2);
            return Integer.parseInt(lastTwo);
        } else if (digits.length() == 1) {
            return Integer.parseInt(digits);
        } else {
            // fallback: use hashcode mod 100
            return Math.abs(regNo.hashCode()) % 100;
        }
    }

    /**
     * Loads the final SQL query from src/main/resources/sql/question{n}.sql
     * Replace contents with your final SQL answers.
     */
    public String loadFinalQuery(int questionNumber) {
        String path = "sql/question" + questionNumber + ".sql";
        try (InputStream is = new ClassPathResource(path).getInputStream();
             Scanner s = new Scanner(is, StandardCharsets.UTF_8)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next().trim() : "";
        } catch (Exception e) {
            throw new RuntimeException("Unable to load SQL file: " + path, e);
        }
    }

    /**
     * Sends finalQuery to the webhook URL using the provided access token in Authorization header.
     * The problem statement requested the token be used as JWT in Authorization header.
     * The spec did not say "Bearer" explicitly, so we send the token directly.
     */
    public void submitFinalQuery(String webhookUrl, String accessToken, String finalQuery) {
        Map<String, String> body = Map.of("finalQuery", finalQuery);

        var resp = webClient.post()
                .uri(webhookUrl) // or testWebhookUrl if they want a fixed URL
                .header("Authorization", accessToken) // sent as-is; if server expects "Bearer <token>", change accordingly
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // resp may be a simple success message; we just log it
        System.out.println("Submission response: " + resp);
    }
}
