package com.alexk.ChatbotGPT.Service;

import com.alexk.ChatbotGPT.dto.TextToSpeechReqDto;
import com.alexk.ChatbotGPT.dto.TextToSpeechRespDto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class TextToSpeechReactService {

    @Value("${openai.elevenlabs.url}")
    private String elevenlabsApiUrl;

    @Value("${openai.elevenlabs.key}") // Load API key from application properties or environment variables
    private String elevenlabsApiKey;

    public Mono<TextToSpeechRespDto> sendTextToSpeechRequestWebFlux(TextToSpeechReqDto textToSpeechReqDto) throws IOException {
//        JSONObject jsonMessage = new JSONObject(textToSpeechReqDto.getMessage());

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(elevenlabsApiUrl);
            httpPost.setHeader("xi-api-key", elevenlabsApiKey);
            httpPost.setHeader("accept", "audio/mpeg");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // Create the JSON payload
            String jsonPayload = "{\n" +
                    "    \"text\": \"" + textToSpeechReqDto.getMessage() + "\",\n" +
                    "    \"model_id\": \"eleven_monolingual_v1\",\n" +
                    "    \"voice_settings\": {\n" +
                    "        \"stability\": 0.5,\n" +
                    "        \"similarity_boost\": 0.5\n" +
                    "    }\n" +
                    "}";

            // Set the JSON payload as the request entity
            HttpEntity httpEntity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(httpEntity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                byte[] responseBodyBytes = EntityUtils.toByteArray(responseEntity);

                TextToSpeechRespDto textToSpeechRespDto = new TextToSpeechRespDto(responseBodyBytes);
                return Mono.just(textToSpeechRespDto);
            } else {
                // Handle error responses here
                return null;
            }
        } finally {
            httpClient.close();
        }
    }
}
