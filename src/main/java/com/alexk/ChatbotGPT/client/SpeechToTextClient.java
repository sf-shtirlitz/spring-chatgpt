package com.alexk.ChatbotGPT.client;

import com.alexk.ChatbotGPT.dto.SpeechToTextReqDto;
import com.alexk.ChatbotGPT.dto.SpeechToTextRespDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SpeechToTextClient {
    private final WebClient webClient;

    public SpeechToTextClient(@Value("${product.service.url}") String url){
        this.webClient = WebClient.builder()
                .baseUrl(url)
                .build();
    }

/*    public Mono<SpeechToTextRespDto> getSpeechToText(SpeechToTextReqDto requestDto){
        return this.webClient
                .get()
                .uri("{id}", productId)
                .retrieve()
                .bodyToMono(SpeechToTextReqDto.class);
    }*/
}
