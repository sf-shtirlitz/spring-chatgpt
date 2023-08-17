package com.alexk.ChatbotGPT.Service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@Service
public class ChatGPTResponseService {
    //POST https://api.openai.com/v1/chat/completions
    @Value("${openai.api.chat.url}")
    private String openApiChatUrl;

    public Mono<ResponseEntity<String>> sendChatGPTRequestWebFlux(String apiKey, String message) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(openApiChatUrl);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            String jsonMessage = prepareJsonMessage(message);
            // Set the JSON content as the request entity
            StringEntity stringEntity = new StringEntity(jsonMessage);
            httpPost.setEntity(stringEntity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String transcript = EntityUtils.toString(responseEntity);
                EntityUtils.consume(responseEntity);
                return Mono.just(ResponseEntity.ok(transcript));
            } else {
                // Handle error responses here
                return null;
            }
        } finally {
            httpClient.close();
        }
    }


    public ResponseEntity<String> sendChatGPTRequest(String apiKey, String message) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(openApiChatUrl);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            String jsonMessage = prepareJsonMessage(message);
            // Set the JSON content as the request entity
            StringEntity stringEntity = new StringEntity(jsonMessage);
            httpPost.setEntity(stringEntity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String transcript = EntityUtils.toString(responseEntity);
                EntityUtils.consume(responseEntity);
                return ResponseEntity.ok(transcript);
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

    private JSONObject prepareInitialJsonMessages(JSONArray messageArray) {
        // Create the JSON object for the overall message
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("model", "gpt-3.5-turbo");
        jsonMessage.put("messages", messageArray);

        // Print the JSON message
        System.out.println(jsonMessage.toString());
        return jsonMessage;
    }
    public Mono<ResponseEntity<String>>  sendInitialAssistantPrepMessages(String apiKey, JSONArray jsonMessageArray) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(openApiChatUrl);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // Set the JSON content as the request entity
            StringEntity stringEntity = new StringEntity(prepareInitialJsonMessages(jsonMessageArray).toString());
            httpPost.setEntity(stringEntity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String transcript = EntityUtils.toString(responseEntity);
                EntityUtils.consume(responseEntity);
                return Mono.just(ResponseEntity.ok(transcript));
            } else {
                return Mono.just(ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("I am a teapot"));
            }
        } catch (UnsupportedEncodingException e) {
            return Mono.just(ResponseEntity.ok(e.toString()));
        } catch (ClientProtocolException e) {
            return Mono.just(ResponseEntity.ok(e.toString()));
        } catch (IOException e) {
            return Mono.just(ResponseEntity.ok(e.toString()));
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
