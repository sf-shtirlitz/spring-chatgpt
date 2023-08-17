package com.alexk.ChatbotGPT.Service;

import com.alexk.ChatbotGPT.dto.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class TextToTextReactService {

    @Value("${openai.api.key}") // Load API key from application properties or environment variables
    private String apiKey;

    @Value("${openai.elevenlabs.key}") // Load API key from application properties or environment variables
    private String textToSpeechApiKey;

    //POST https://api.openai.com/v1/chat/completions
    @Value("${openai.api.chat.url}")
    private String openApiChatUrl;
    public Mono<TextToTextRespDto> sendChatGPTRequestWebFlux(TextToTextReqDto textToTextReqDto) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(openApiChatUrl);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            String jsonMessage = prepareJsonMessage(textToTextReqDto.getConvertedText());
            // Set the JSON content as the request entity
            StringEntity stringEntity = new StringEntity(jsonMessage);
            httpPost.setEntity(stringEntity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String transcript = EntityUtils.toString(responseEntity);
                EntityUtils.consume(responseEntity);

                JSONObject jsonObject = new JSONObject(transcript);
                String botResponse = ((JSONObject)(jsonObject.getJSONArray("choices").getJSONObject(0).get("message")))
                        .getString("content");
                System.out.println("ChatGPT response: " + botResponse);

                TextToTextRespDto textToTextRespDto = new TextToTextRespDto(botResponse);
                return Mono.just(textToTextRespDto);
            } else {
                // Handle error responses here
                return null;
            }
        } finally {
            httpClient.close();
        }
    }

    private String prepareJsonMessage(String message) {
        // Create the JSON object for the "messages" array
        JSONObject messageObject = new JSONObject();
        messageObject.put("role", "user");
        messageObject.put("content", message);

        // Create the "messages" array and add the message object
        JSONArray messagesArray = new JSONArray();
        messagesArray.put(messageObject);

        // Create the JSON object for the overall message
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("model", "gpt-3.5-turbo");
        jsonMessage.put("messages", messagesArray);

        // Print the JSON message
        System.out.println(jsonMessage.toString());
        return jsonMessage.toString();
    }
}
