package com.mycompany.webhookapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.client.RestTemplate;

@Component
public class WebhookRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        // Step 1: Call /generateWebhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

        String body = """
        {
            "name": "John Doe",
            "regNo": "REG12347",
            "email": "john@example.com"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Webhook response: " + response.getBody());

        // Step 2: Parse response
        JsonNode root = objectMapper.readTree(response.getBody());
        String webhookUrl = root.get("webhook").asText();
        String accessToken = root.get("accessToken").asText();
        JsonNode usersNode = root.get("data");

        // Step 3: Solve the Nth-Level Followers problem
        JsonNode outcome = FollowerSolver.solve(usersNode);

        // Step 4: Send result to webhook with JWT
        HttpHeaders webhookHeaders = new HttpHeaders();
        webhookHeaders.setContentType(MediaType.APPLICATION_JSON);
        webhookHeaders.set("Authorization", accessToken);

        // Prepare final result
        ObjectNode finalPayload = objectMapper.createObjectNode();
        finalPayload.put("regNo", "REG12347");
        finalPayload.set("outcome", outcome);

        HttpEntity<String> webhookRequest = new HttpEntity<>(finalPayload.toString(), webhookHeaders);

        // Retry up to 4 times
        for (int i = 1; i <= 4; i++) {
            try {
                ResponseEntity<String> webhookResponse = restTemplate.postForEntity(webhookUrl, webhookRequest, String.class);
                System.out.println("✅ Webhook call succeeded on attempt " + i);
                break;
            } catch (Exception e) {
                System.out.println("❌ Attempt " + i + " failed: " + e.getMessage());
                if (i == 4) throw e; // throw on final failure
                Thread.sleep(1000); // wait before retry
            }
        }
    }
}
