package com.alexk.ChatbotGPT.Service;

import com.alexk.ChatbotGPT.dto.TextToSpeechReqDto;
import com.sun.net.httpserver.Request;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

@Service
public class TextToSpeechService {

    @Value("${openai.elevenlabs.url}")
    private String elevenlabsApiUrl;

    @Value("${openai.api.key}") // Load API key from application properties or environment variables
    private String apiKey;

    public ResponseEntity<byte[]> sendTextToSpeechRequest(String apiKey, String message) throws IOException {
//        JSONObject jsonMessage = new JSONObject(message);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(elevenlabsApiUrl);
            httpPost.setHeader("xi-api-key", apiKey);
            httpPost.setHeader("accept", "audio/mpeg");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // Create the JSON payload
            String jsonPayload = "{\n" +
                    "    \"text\": \"" + message + "\",\n" +
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

                return ResponseEntity.ok(responseBodyBytes);
            } else {
                // Handle error responses here
                return null;
            }
        } finally {
            httpClient.close();
        }
    }

    public Mono<ResponseEntity<byte[]>> sendTextToSpeechRequestWebFlux(String message) throws IOException {
        JSONObject jsonMessage = new JSONObject(message);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(elevenlabsApiUrl);
            httpPost.setHeader("xi-api-key", apiKey);
            httpPost.setHeader("accept", "audio/mpeg");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // Create the JSON payload
            String jsonPayload = "{\n" +
                    "    \"text\": \"" + jsonMessage.getString("text") + "\",\n" +
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

                return Mono.just(ResponseEntity.ok(responseBodyBytes));
            } else {
                // Handle error responses here
                return null;
            }
        } finally {
            httpClient.close();
        }
    }
}
