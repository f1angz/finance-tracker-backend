package com.finance.tracker.service;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import java.util.List;

//Клиент для отправки запроса к ИИ-модели DeepSeek
@Component
public class DeepSeekClient {

    private final RestClient restClient;

    public DeepSeekClient(
            @Value("${app.deepseek.api-key}") String apiKey,
            @Value("${app.deepseek.base-url:https://api.deepseek.com}") String baseUrl
    ) throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .setProtocol("TLSv1.2")
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setSSLSocketFactory(
                                        SSLConnectionSocketFactoryBuilder.create()
                                                .setSslContext(sslContext)
                                                .build()
                                )
                                .build()
                )
                .build();

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** Обычный текстовый ответ */
    public String chat(String systemPrompt, String userMessage) {
        return send(new ChatCompletionRequest("deepseek-chat",
                List.of(new Message("system", systemPrompt), new Message("user", userMessage)),
                null));
    }

    /** Ответ в формате JSON (response_format: json_object) */
    public String chatJson(String systemPrompt, String userMessage) {
        return send(new ChatCompletionRequest("deepseek-chat",
                List.of(new Message("system", systemPrompt), new Message("user", userMessage)),
                new ResponseFormat("json_object")));
    }

    private String send(ChatCompletionRequest request) {
        ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new RuntimeException("Empty response from DeepSeek API");
        }
        return response.choices().get(0).message().content();
    }

    record ChatCompletionRequest(String model, List<Message> messages, ResponseFormat response_format) {}
    record Message(String role, String content) {}
    record ResponseFormat(String type) {}
    record ChatCompletionResponse(List<Choice> choices) {}
    record Choice(Message message) {}
}
