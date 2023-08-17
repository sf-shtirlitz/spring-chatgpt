package com.alexk.ChatbotGPT.Service;

import com.alexk.ChatbotGPT.dto.SpeechToTextReqDto;
import com.alexk.ChatbotGPT.dto.SpeechToTextRespDto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class SpeechToTextReactService {

    //"https://api.openai.com/v1/audio/transcriptions";
    @Value("${openai.api.url}")
    private String openApiUrl;

    @Value("${openai.api.key}") // Load API key from application properties or environment variables
    private String apiKey;
    public Mono<ResponseEntity<String>> sendTranscriptionRequestWebFlux(String apiKey, InputStream audioInputStream, String fileName) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(openApiUrl);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", audioInputStream, ContentType.APPLICATION_OCTET_STREAM, fileName);
            builder.addTextBody("model", "whisper-1");

            HttpEntity multipartEntity = builder.build();
            httpPost.setEntity(multipartEntity);

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

    public Mono<SpeechToTextRespDto> sendTranscriptionRequestWebFluxViaDto(SpeechToTextReqDto reqDto) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(openApiUrl);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", new ByteArrayInputStream(reqDto.getSpeechMessage()), ContentType.APPLICATION_OCTET_STREAM,reqDto.getFileName());
            builder.addTextBody("model", "whisper-1");

            HttpEntity multipartEntity = builder.build();
            httpPost.setEntity(multipartEntity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String transcript = EntityUtils.toString(responseEntity);
                EntityUtils.consume(responseEntity);

                JSONObject jsonObject = new JSONObject(transcript);

                SpeechToTextRespDto respDto = new SpeechToTextRespDto(jsonObject.getString("text"));
                return Mono.just(respDto);
            } else {
                // Handle error responses here
                return null;
            }
        } finally {
            httpClient.close();
        }
    }
}
